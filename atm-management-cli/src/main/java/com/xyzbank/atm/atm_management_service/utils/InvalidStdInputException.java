package com.xyzbank.atm.atm_management_service.utils;

public class InvalidStdInputException extends Exception {

    public InvalidStdInputException() {
        super("You have entered invalid Command!");
    }

    public InvalidStdInputException(String message) {
        super(message);
    }
}
