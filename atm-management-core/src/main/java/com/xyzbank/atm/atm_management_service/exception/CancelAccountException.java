package com.xyzbank.atm.atm_management_service.exception;

public class CancelAccountException extends Exception {

    private static final long serialVersionUID = 1L;

    public CancelAccountException() {
        super("Cancel account failed");
    }

    public CancelAccountException(String message) {
        super(message);
    }
}
