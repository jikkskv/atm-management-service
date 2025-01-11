package com.xyzbank.atm.atm_management_service.service.impl;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.exception.DepositOperationException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.exception.TransferOperationException;
import com.xyzbank.atm.atm_management_service.exception.WithdrawOperationException;
import com.xyzbank.atm.atm_management_service.service.AccountTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service("accountTransactionalService")
@Slf4j
public class AccountTransactionalServiceImpl implements AccountTransactionalService {

    @Autowired
    private AccountDao accountDao;

    private static final int REVERT_ATTEMPTS = 5;

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
            if (account.getBalance().compareTo(amount) < 1)
                throw new WithdrawOperationException("Insufficient balance");

            int updatedRows = accountDao.updateBalanceWithVersion(account.getAccountId(), account.getBalance().subtract(amount), account.getVersionId());
            log.info("End of withdraw, accountId: {}, amount: {}, remarks: {}, updatedRows: {}", accountId, amount, remarks, updatedRows);
            if (updatedRows != 1) {
                throw new WithdrawOperationException();
            }
        } catch (RuntimeException | WithdrawOperationException ex) {
            log.error("Error in withdraw, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks, ex);
            throw new WithdrawOperationException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in withdraw, accountId: {}, amount: {}, remarks: {}", accountId, amount, remarks, ex);
            throw new WithdrawOperationException();
        }
    }

    private Account getAccountAfterValidation(Long accountId) throws InvalidAccountException {
        Optional<Account> optionalAccount = accountDao.findById(accountId);
        Account account = optionalAccount.orElseThrow(InvalidAccountException::new);
        if (!account.getAccountStatus().equals(AccountStatus.AVAILABLE)) throw new InvalidAccountException();
        return account;
    }
}
