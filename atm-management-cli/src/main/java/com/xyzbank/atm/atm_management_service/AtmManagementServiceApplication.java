package com.xyzbank.atm.atm_management_service;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.debt.DebtBalance;
import com.xyzbank.atm.atm_management_service.exception.*;
import com.xyzbank.atm.atm_management_service.model.AccountRequestModel;
import com.xyzbank.atm.atm_management_service.model.CreateAccountRequestModel;
import com.xyzbank.atm.atm_management_service.service.AccountCrudService;
import com.xyzbank.atm.atm_management_service.service.AccountTransactionalService;
import com.xyzbank.atm.atm_management_service.service.impl.UserService;
import com.xyzbank.atm.atm_management_service.user.User;
import com.xyzbank.atm.atm_management_service.utils.AccountState;
import com.xyzbank.atm.atm_management_service.utils.CommandLineInputText;
import com.xyzbank.atm.atm_management_service.utils.InvalidStdInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@Slf4j
public class AtmManagementServiceApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountCrudService accountCrudService;

    @Autowired
    private AccountTransactionalService accountTransactionalService;

    private static final AccountState accountState = new AccountState();

    public static void main(String[] args) {
        SpringApplication.run(AtmManagementServiceApplication.class, args);
        log.info("AtmManagementServiceApplication has been started.");
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner cmdLineScanner = new Scanner(System.in);
        System.out.println(String.format(CommandLineInputText.WELCOME_MESSAGE));
        while (true) {
            String inputCommand = null;
            try {
                inputCommand = cmdLineScanner.nextLine().trim();
                processCmdLineInput(inputCommand);
            } catch (Exception ex) {
                log.error("Error occurred ine executing the command: {} with exception: ", inputCommand, ex);
                System.out.println(ex.getMessage());
            }
        }
    }

    private void processCmdLineInput(String inputCommand) {
        try {
            String[] inputStrArr = inputCommand.split(" ");
            if (inputStrArr.length == 0) throw new InvalidStdInputException();
            String commandName = inputStrArr[0].toLowerCase();
            switch (inputStrArr[0]) {
                case "login": {
                    performLoginOperation(inputStrArr);
                    break;
                }
                case "deposit": {
                    performDepositOperation(inputStrArr);
                    break;
                }
                case "withdraw": {
                    performWithdrawOperation(inputStrArr);
                    break;
                }
                case "transfer": {
                    performTransferOperation(inputStrArr);
                    break;
                }
                case "logout": {
                    User currUser = accountState.getUser();
                    if (Objects.isNull(currUser)) {
                        System.out.println("No User is logged in!");
                    } else {
                        accountState.setAccount(null);
                        accountState.setUser(null);
                        System.out.println(String.format(CommandLineInputText.EXIT_MESSAGE, currUser.getName()));
                    } break;
                }
                default: {
                    System.out.println("You have entered invalid Command!");
                }
            }
        } catch (InvalidStdInputException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void performWithdrawOperation(String[] inputStrArr) throws InvalidStdInputException {
        Account currAccount = accountState.getAccount();
        if (Objects.isNull(currAccount)) throw new InvalidStdInputException(CommandLineInputText.LOGIN_WARNING_MESSAGE);
        if (inputStrArr.length != 2) throw new InvalidStdInputException();
        try {
            BigDecimal amount = new BigDecimal(inputStrArr[1]);
            try {
                accountTransactionalService.withdraw(currAccount.getAccountId(), amount, "");
                displayBalance();
                showDebt();
            } catch (WithdrawOperationException ex) {
                System.out.println(ex.getMessage());
                log.error("Error occurred in deposit operation: {} with inputStrArr: ", Arrays.toString(inputStrArr), ex);
            }
        } catch (NumberFormatException ex) {
            throw new InvalidStdInputException();
        }
    }

    private void performLoginOperation(String[] inputStrArr) throws InvalidStdInputException {
        if (inputStrArr.length != 2 || !StringUtils.hasLength(inputStrArr[1])) {
            throw new InvalidStdInputException();
        }
        try {
            Account account = accountCrudService.getOrCreateAccount(new CreateAccountRequestModel(inputStrArr[1], "", ""));
            Optional<User> optionalUser = userService.getUser(account.getUserId());
            accountState.setAccount(account);
            optionalUser.ifPresent(accountState::setUser);
            System.out.println(String.format(CommandLineInputText.WELCOME_USER_MESSAGE, inputStrArr[1]));
            displayBalance();
            showDebt();
        } catch (CreateAccountException ex) {
            log.error("Error occurred in getOrCreateAccount: {} with inputStrArr: ", Arrays.toString(inputStrArr), ex);
        }
    }

    private void performDepositOperation(String[] inputStrArr) throws InvalidStdInputException {
        Account currAccount = accountState.getAccount();
        if (Objects.isNull(currAccount)) throw new InvalidStdInputException(CommandLineInputText.LOGIN_WARNING_MESSAGE);
        if (inputStrArr.length != 2) throw new InvalidStdInputException();
        try {
            BigDecimal amount = new BigDecimal(inputStrArr[1]);
            try {
                accountTransactionalService.deposit(currAccount.getAccountId(), amount, "");
                displayBalance();
                showDebt();
            } catch (DepositOperationException ex) {
                log.error("Error occurred in deposit operation: {} with inputStrArr: ", Arrays.toString(inputStrArr), ex);
            }
        } catch (NumberFormatException ex) {
            throw new InvalidStdInputException();
        }
    }

    private void performTransferOperation(String[] inputStrArr) throws InvalidStdInputException {
        Account currAccount = accountState.getAccount();
        if (Objects.isNull(currAccount)) throw new InvalidStdInputException(CommandLineInputText.LOGIN_WARNING_MESSAGE);
        if (inputStrArr.length != 3 || !StringUtils.hasLength(inputStrArr[1])) throw new InvalidStdInputException();
        try {
            Account toAccount = accountCrudService.getOrCreateAccount(new CreateAccountRequestModel(inputStrArr[1], "", ""));
            if ((long) currAccount.getAccountId() == toAccount.getAccountId())
                throw new InvalidStdInputException("Cannot transfer to same account");
            try {
                BigDecimal amount = new BigDecimal(inputStrArr[2]);
                accountTransactionalService.transfer(currAccount.getAccountId(), toAccount.getAccountId(), amount, "transfer");
                displayBalance();
                showDebt();
            } catch (NumberFormatException ex) {
                throw new InvalidStdInputException();
            }
        } catch (InvalidAccountException | TransferOperationException | CreateAccountException ex) {
            log.error("Error occurred in transfer operation: {} with inputStrArr: ", Arrays.toString(inputStrArr), ex);
        }
    }

    private void showDebt() {
        try {
            List<DebtBalance> debtBalanceList = accountCrudService.getAllDebts(new AccountRequestModel(accountState.getUser().getName()));
            debtBalanceList.forEach(e -> {
                if (e.getFromAccountId() == accountState.getAccount().getAccountId()) {
                    Optional<User> user = accountCrudService.getUser(e.getToAccountId());
                    System.out.println(String.format(CommandLineInputText.OWED_TO_MESSAGE, e.getOutStandingBalance().abs(), user.get().getName()));
                } else {
                    Optional<User> user = accountCrudService.getUser(e.getFromAccountId());
                    System.out.println(String.format(CommandLineInputText.OWED_FROM_MESSAGE, e.getOutStandingBalance().abs(), user.get().getName()));
                }
            });
        } catch (InvalidAccountException ex) {
            log.error("Error occurred in displaying balance of user: {} : ", accountState.getUser(), ex);
        }
    }

    private void displayBalance() {
        try {
            BigDecimal balance = accountCrudService.getBalance(new AccountRequestModel(accountState.getUser().getName()));
            System.out.println(String.format(CommandLineInputText.BALANCE_MESSAGE, balance));
        } catch (InvalidAccountException ex) {
            log.error("Error occurred in displaying balance of user: {} : ", accountState.getUser(), ex);
        }
    }
}
