package com.xyzbank.atm.atm_management_service.exception;

public class InvalidAccountException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidAccountException() {
        super("Invalid account id");
    }

    public InvalidAccountException(String message) {
        super(message);
    }
}
