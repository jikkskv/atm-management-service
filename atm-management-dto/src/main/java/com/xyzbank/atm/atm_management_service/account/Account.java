package com.xyzbank.atm.atm_management_service.account;

import com.xyzbank.atm.atm_management_service.transaction.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Account {

    @Getter
    @Setter
    private Long accountId;

    @Getter
    private volatile BigDecimal balance;

    private String currency;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @Getter
    private List<Transaction> transactions;

    @Getter
    @Setter
    private Long userId;

    @Setter
    private AccountStatus accountStatus;

    public Account() {
        balance = BigDecimal.ZERO;
        accountStatus = AccountStatus.AVAILABLE;
        transactions = Collections.emptyList();
    }
}
