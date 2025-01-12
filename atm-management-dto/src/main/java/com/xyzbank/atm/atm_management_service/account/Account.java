package com.xyzbank.atm.atm_management_service.account;

import com.xyzbank.atm.atm_management_service.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @NotNull
    @Column(name = "balance", nullable = false)
    private volatile BigDecimal balance;

    @NotEmpty
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

//    private List<Transaction> transactions;  //TODO: do jpa binding here

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Version
    @Column(name = "version_id", nullable = false)
    private Integer versionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    public List<Transaction> getTransactions() { //TODO: remove after doing jpa binding
        return Collections.emptyList();
    }
}
