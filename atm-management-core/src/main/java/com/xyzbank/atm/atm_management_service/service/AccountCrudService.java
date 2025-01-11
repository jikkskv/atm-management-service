package com.xyzbank.atm.atm_management_service.service;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.exception.CancelAccountException;
import com.xyzbank.atm.atm_management_service.exception.CreateAccountException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.model.AccountRequestModel;
import com.xyzbank.atm.atm_management_service.model.CreateAccountRequestModel;
import com.xyzbank.atm.atm_management_service.transaction.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface AccountCrudService {

    Account createAccount(CreateAccountRequestModel createAccountRequest) throws CreateAccountException;

    boolean cancelAccount(AccountRequestModel accountRequestModel) throws CancelAccountException;

    List<Transaction> getTransaction(AccountRequestModel accountRequestModel) throws InvalidAccountException;

    BigDecimal getBalance(AccountRequestModel accountRequestModel) throws InvalidAccountException;
}
