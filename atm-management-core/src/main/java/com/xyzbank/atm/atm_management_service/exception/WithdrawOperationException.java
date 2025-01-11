package com.xyzbank.atm.atm_management_service.exception;

public class WithdrawOperationException extends Exception {

    private static final long serialVersionUID = 1L;

    public WithdrawOperationException() {
        super("Withdraw operation failed");
    }

    public WithdrawOperationException(String message) {
        super(message);
    }
}
