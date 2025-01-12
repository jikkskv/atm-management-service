package com.xyzbank.atm.atm_management_service.utils;

public class CommandLineInputText {

    private CommandLineInputText() {
    }

    public static final String WELCOME_MESSAGE = "Welcome to ATM application, Please enter your command!\n";

    public static final String WELCOME_USER_MESSAGE = "Hello, %s!";

    public static final String BALANCE_MESSAGE = "Your balance is $%s";

    public static final String OWED_TO_MESSAGE = "Owed $%s to %s";

    public static final String OWED_FROM_MESSAGE = "Owed $%s from %s";

    public static final String LOGIN_WARNING_MESSAGE = "Please login first to perform ATM operation";

    public static final String EXIT_MESSAGE = "Goodbye, %s!";
}
