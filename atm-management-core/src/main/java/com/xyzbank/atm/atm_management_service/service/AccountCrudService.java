package com.xyzbank.atm.atm_management_service.service;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.debt.DebtBalance;
import com.xyzbank.atm.atm_management_service.exception.CancelAccountException;
import com.xyzbank.atm.atm_management_service.exception.CreateAccountException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.model.AccountRequestModel;
import com.xyzbank.atm.atm_management_service.model.CreateAccountRequestModel;
import com.xyzbank.atm.atm_management_service.transaction.Transaction;
import com.xyzbank.atm.atm_management_service.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountCrudService {

    Account getOrCreateAccount(CreateAccountRequestModel createAccountRequest) throws CreateAccountException;

    boolean cancelAccount(AccountRequestModel accountRequestModel) throws CancelAccountException;

    Optional<User> getUser(Long accountId);

    List<Transaction> getTransaction(AccountRequestModel accountRequestModel) throws InvalidAccountException;

    List<DebtBalance> getAllDebts(AccountRequestModel accountRequestModel) throws InvalidAccountException;

    BigDecimal getBalance(AccountRequestModel accountRequestModel) throws InvalidAccountException;
}
