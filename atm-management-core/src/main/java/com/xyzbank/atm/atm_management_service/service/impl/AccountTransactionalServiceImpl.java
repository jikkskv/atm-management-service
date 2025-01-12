package com.xyzbank.atm.atm_management_service.service.impl;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.dao.DebtBalanceDao;
import com.xyzbank.atm.atm_management_service.dao.UserDao;
import com.xyzbank.atm.atm_management_service.debt.DebtBalance;
import com.xyzbank.atm.atm_management_service.exception.DepositOperationException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.exception.TransferOperationException;
import com.xyzbank.atm.atm_management_service.exception.WithdrawOperationException;
import com.xyzbank.atm.atm_management_service.service.AccountTransactionalService;
import com.xyzbank.atm.atm_management_service.service.DebtRestructuringService;
import com.xyzbank.atm.atm_management_service.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service("accountTransactionalService")
@Slf4j
public class AccountTransactionalServiceImpl implements AccountTransactionalService {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DebtBalanceDao debtBalanceDao;

    @Autowired
    private DebtRestructuringService debtRestructuringService;

    @Lazy
    @Autowired
    private AccountTransactionalServiceImpl self;

    private static final int REVERT_ATTEMPTS = 5;

    public static final String TRANSFERRED_MESSAGE = "Transferred $%s to %s";

    @Override
    public void deposit(Long accountId, BigDecimal amount, String remarks) throws DepositOperationException {
        log.info("Start of deposit, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks);
        try {
            if (amount.compareTo(BigDecimal.ZERO) < 1) throw new DepositOperationException("Invalid Amount");
            Account account = getAccountAfterValidation(accountId);
            int updatedRows = accountDao.updateBalanceWithVersion(account.getAccountId(), account.getBalance().add(amount), account.getVersionId());
            log.info("End of deposit, accountId: {}, amount: {}, remarks: {}, updatedRows: {}", accountId, amount, remarks, updatedRows);
            if (updatedRows != 1) {
                throw new DepositOperationException();
            }
            clearDebtBalancesIfAny(account, amount);
            debtRestructuringService.restructureDebt();
        } catch (RuntimeException | DepositOperationException ex) {
            log.error("Error in deposit, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks, ex);
            throw new DepositOperationException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in deposit, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks, ex);
            throw new DepositOperationException();
        }
    }

    @Override
    public void withdraw(Long accountId, BigDecimal amount, String remarks) throws WithdrawOperationException {
        log.info("Start of withdraw, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks);
        try {
            Account account = getAccountAfterValidation(accountId);
            if (account.getBalance().compareTo(amount) < 0)
                throw new WithdrawOperationException("Insufficient balance");

            int updatedRows = accountDao.updateBalanceWithVersion(account.getAccountId(), account.getBalance().subtract(amount), account.getVersionId());
            log.info("End of withdraw, accountId: {}, amount: {}, remarks: {}, updatedRows: {}", accountId, amount, remarks, updatedRows);
            if (updatedRows != 1) {
                throw new WithdrawOperationException();
            }
            debtRestructuringService.restructureDebt();
        } catch (RuntimeException | WithdrawOperationException ex) {
            log.error("Error in withdraw, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks, ex);
            throw new WithdrawOperationException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in withdraw, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks, ex);
            throw new WithdrawOperationException();
        }
    }

    @Override
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String remarks) throws InvalidAccountException, TransferOperationException {
        log.info("Start of transfer, fromAccountId: {}, toAccountId: {}, amount: {}, remarks: {}", fromAccountId, toAccountId, amount, remarks);
        try {
            Account fromAccount = getAccountAfterValidation(fromAccountId);
            Account toAccount = getAccountAfterValidation(toAccountId);
            if (!checkAndAddOutStandingBalance(fromAccountId, toAccountId, amount)) {
                BigDecimal transferredAmount = self.performTransferOperationWithTransaction(fromAccount, toAccount, amount, remarks);
                User user = userDao.findById(toAccount.getUserId()).get();
                System.out.println(String.format(TRANSFERRED_MESSAGE, transferredAmount, user.getName()));
            }
            debtRestructuringService.restructureDebt();
        } catch (TransferOperationException | RuntimeException | InvalidAccountException ex) {
            log.error("Error in transfer, fromAccountId: {}, toAccountId: {}, amount: {}, remarks: {}", fromAccountId, toAccountId, amount, remarks, ex);
            throw new TransferOperationException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in transfer, fromAccountId: {}, toAccountId: {}, amount: {}, remarks: {}", fromAccountId, toAccountId, amount, remarks, ex);
            throw new TransferOperationException();
        }
    }

    @Transactional
    public BigDecimal performTransferOperationWithTransaction(Account fromAccount, Account toAccount, BigDecimal amount, String remarks) throws TransferOperationException {
        BigDecimal balancedAmount = fromAccount.getBalance().compareTo(amount) < 0 ? fromAccount.getBalance() : amount;
        BigDecimal withdrawnAmount = fromAccount.getBalance().subtract(balancedAmount);
        int withdrawnRows = accountDao.updateBalanceWithVersion(fromAccount.getAccountId(), withdrawnAmount, fromAccount.getVersionId());
        log.info("transfer operation, fromAccountId: {}, toAccountId: {}, balancedAmount: {}, amount: {}, remarks: {}, withdrawnRows: {}", fromAccount.getAccountId(), toAccount.getAccountId(), balancedAmount, amount, remarks, withdrawnRows);
        if (withdrawnRows == 1) {
            BigDecimal depositedAmount = toAccount.getBalance().add(balancedAmount);
            int depositedRows = accountDao.updateBalanceWithVersion(toAccount.getAccountId(), depositedAmount, toAccount.getVersionId());
            log.info("End of transfer, fromAccountId: {}, toAccountId: {}, balancedAmount: {}, amount: {}, remarks: {}, depositedRows: {}", fromAccount.getAccountId(), toAccount.getAccountId(), balancedAmount, amount, remarks, depositedRows);
            if (depositedRows != 1) {
                throw new TransferOperationException();
            }
            self.addDebtIfNeeded(fromAccount, toAccount, amount.subtract(balancedAmount));
        } else {
            throw new TransferOperationException();
        }
        return balancedAmount;
    }

    private boolean checkAndAddOutStandingBalance(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        List<DebtBalance> debtBalanceList = debtBalanceDao.findByFromAccountIdAndToAccountId(toAccountId, fromAccountId);
        if (!CollectionUtils.isEmpty(debtBalanceList)) {
            debtBalanceDao.saveAndFlush(DebtBalance.buildDebtBalanceObject(fromAccountId, toAccountId, amount));
        }
        return !CollectionUtils.isEmpty(debtBalanceList);
    }

    private BigDecimal clearDebtBalancesIfAny(Account account, BigDecimal amount) throws TransferOperationException, InvalidAccountException {
        BigDecimal remainingAmount = amount;
        List<DebtBalance> debtBalances = debtBalanceDao.findByFromAccountId(account.getAccountId());
        for (DebtBalance debtBalance : debtBalances) {
            if (debtBalance.getOutStandingBalance().compareTo(BigDecimal.ZERO) < 0) {
                boolean isOverFlow = remainingAmount.compareTo(debtBalance.getOutStandingBalance().abs()) < 0;
                BigDecimal withDrawnAmount = isOverFlow ? remainingAmount : debtBalance.getOutStandingBalance().abs();
                transfer(debtBalance.getFromAccountId(), debtBalance.getToAccountId(), withDrawnAmount, "");
                debtBalanceDao.saveAndFlush(DebtBalance.buildDebtBalanceObject(debtBalance.getToAccountId(), debtBalance.getFromAccountId(), remainingAmount));
                remainingAmount = remainingAmount.subtract(withDrawnAmount);
                if (isOverFlow) break;
            }
        }
        return remainingAmount;
    }

    public void addDebtIfNeeded(Account fromAccount, Account toAccount, BigDecimal debtAmount) {
        if (debtAmount.compareTo(BigDecimal.ZERO) != 0) {
            debtBalanceDao.saveAndFlush(DebtBalance.buildDebtBalanceObject(fromAccount.getAccountId(), toAccount.getAccountId(), debtAmount));
        }
    }

    private Account getAccountAfterValidation(Long accountId) throws InvalidAccountException {
        Optional<Account> optionalAccount = accountDao.findById(accountId);
        Account account = optionalAccount.orElseThrow(InvalidAccountException::new);
        if (!account.getAccountStatus().equals(AccountStatus.AVAILABLE)) throw new InvalidAccountException();
        return account;
    }
}
