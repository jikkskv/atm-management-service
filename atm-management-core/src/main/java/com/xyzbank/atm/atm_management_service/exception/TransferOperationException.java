package com.xyzbank.atm.atm_management_service.exception;

public class TransferOperationException extends Exception {

    private static final long serialVersionUID = 1L;

    public TransferOperationException() {
        super("Transfer operation failed");
    }

    public TransferOperationException(String message) {
        super(message);
    }
}
