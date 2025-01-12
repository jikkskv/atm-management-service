package com.xyzbank.atm.atm_management_service.service;

import com.xyzbank.atm.atm_management_service.exception.DepositOperationException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.exception.TransferOperationException;
import com.xyzbank.atm.atm_management_service.exception.WithdrawOperationException;

import java.math.BigDecimal;

public interface AccountTransactionalService {

    void deposit(Long accountId, BigDecimal amount, String remarks) throws DepositOperationException;

    void withdraw(Long accountId, BigDecimal amount, String remarks) throws WithdrawOperationException;

    void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String remarks) throws InvalidAccountException, TransferOperationException;
}
