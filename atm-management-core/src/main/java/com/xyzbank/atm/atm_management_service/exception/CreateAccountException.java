package com.xyzbank.atm.atm_management_service.exception;

public class CreateAccountException extends Exception {

    private static final long serialVersionUID = 1L;

    public CreateAccountException() {
        super("Create account failed");
    }

    public CreateAccountException(String message) {
        super(message);
    }
}
