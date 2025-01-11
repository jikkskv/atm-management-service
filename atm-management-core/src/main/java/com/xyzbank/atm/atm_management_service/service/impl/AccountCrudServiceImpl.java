package com.xyzbank.atm.atm_management_service.service.impl;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.dao.UserDao;
import com.xyzbank.atm.atm_management_service.exception.CancelAccountException;
import com.xyzbank.atm.atm_management_service.exception.CreateAccountException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.model.AccountRequestModel;
import com.xyzbank.atm.atm_management_service.model.CreateAccountRequestModel;
import com.xyzbank.atm.atm_management_service.service.AccountCrudService;
import com.xyzbank.atm.atm_management_service.transaction.Transaction;
import com.xyzbank.atm.atm_management_service.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service("accountCrudService")
@Slf4j
public class AccountCrudServiceImpl implements AccountCrudService {

    private static final String DEFAULT_CURRENCY = "USD";

    @Autowired
    private UserDao userDao;

    @Autowired
    private AccountDao accountDao;

    @Override
    public Account createAccount(CreateAccountRequestModel createAccountRequest) throws CreateAccountException {
        try {
            log.info("Start of validateAccountInfo, createAccountRequest: {}", createAccountRequest);
            boolean validationStatus = validateAccountInfo(createAccountRequest);
            log.info("End of validateAccountInfo, createAccountRequest: {}, validationStatus: {}", createAccountRequest, validationStatus);
            if (validationStatus) {
                Account createdAccount = getAccount(createAccountRequest);
                if (Objects.isNull(createdAccount)) {
                    throw new CreateAccountException("Create Account failed");
                }
                return createdAccount;
            } else {
                log.error("Error in createAccount, createAccountRequest: {}, invalid input", createAccountRequest);
                throw new CreateAccountException("Invalid input for account creation");
            }
        } catch (CreateAccountException ex) {
            log.error("Error in createAccount, createAccountRequest: {}, CreateAccountException", createAccountRequest, ex);
            throw new CreateAccountException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in createAccount, createAccountRequest: {}, Exception", createAccountRequest, ex);
            throw new CreateAccountException();
        }
    }

    private Account getAccount(CreateAccountRequestModel createAccountRequest) {
        Optional<Account> optionalAccount = getAccountAfterExistsCheck(createAccountRequest.name());
        return optionalAccount.orElseGet(() -> createUserAndAccount(createAccountRequest));
    }

    private Account createUserAndAccount(CreateAccountRequestModel createAccountRequest) {
        User createdUser = saveUser(createAccountRequest);
        return saveAccount(createdUser);
    }

    private Account saveAccount(User user) {
        log.info("Start of saveAccount, user: {}", user);
        Account account = Account.builder()
                .balance(BigDecimal.ZERO)
                .currency(DEFAULT_CURRENCY)
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .build();
        return accountDao.saveAndFlush(account);
    }

    private User saveUser(CreateAccountRequestModel createAccountRequest) {
        User user = User.builder().name(createAccountRequest.name()).build();
        return userDao.saveAndFlush(user);
    }

    private Optional<Account> getAccountAfterExistsCheck(String userName) {
        User user = userDao.findByName(userName);
        if (Objects.isNull(user) || Objects.isNull(user.getId())) return Optional.empty();

        Account account = accountDao.findByUserId(user.getId());
        if (Objects.isNull(account)) return Optional.empty();
        return Optional.of(account);
    }

    @Override
    public boolean cancelAccount(AccountRequestModel accountRequestModel) throws CancelAccountException {
        try {
            log.info("cancelAccount request, accountRequestModel: {}", accountRequestModel);
            if (!validateAccountRequestModel(accountRequestModel)) {
                log.error("Error in cancelAccount, accountRequestModel: {}, invalid input", accountRequestModel);
                throw new CancelAccountException("Invalid input for account creation");
            }

            Optional<Account> optionalAccount = getAccountAfterExistsCheck(accountRequestModel.name());
            optionalAccount.ifPresent((act) -> {
                accountDao.updateAccountStatus(act.getAccountId(), AccountStatus.CANCELED, act.getVersionId());
            });
            return true;
        } catch (Exception ex) {
            log.info("Error in cancelAccount, accountRequestModel: {}, Exception:", accountRequestModel, ex);
            throw new CancelAccountException();
        }
    }

    @Override
    public List<Transaction> getTransaction(AccountRequestModel accountRequestModel) throws InvalidAccountException {
        if (validateAccountRequestModel(accountRequestModel)) {
            Optional<Account> optionalAccount = getAccountAfterExistsCheck(accountRequestModel.name());
            return optionalAccount.orElseThrow(InvalidAccountException::new).getTransactions();
        }
        throw new InvalidAccountException();
    }

    @Override
    public BigDecimal getBalance(AccountRequestModel accountRequestModel) throws InvalidAccountException {
        if (validateAccountRequestModel(accountRequestModel)) {
            Optional<Account> optionalAccount = getAccountAfterExistsCheck(accountRequestModel.name());
            return optionalAccount.orElseThrow(InvalidAccountException::new).getBalance();
        }
        throw new InvalidAccountException();
    }

    private boolean validateAccountInfo(CreateAccountRequestModel createAccountRequest) {
        return Objects.nonNull(createAccountRequest.name()) && !createAccountRequest.name().isBlank();
    }

    private boolean validateAccountRequestModel(AccountRequestModel accountRequestModel) {
        return Objects.nonNull(accountRequestModel.name()) && !accountRequestModel.name().isBlank();
    }
}
