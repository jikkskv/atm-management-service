package com.xyzbank.atm.atm_management_service.account;

import com.xyzbank.atm.atm_management_service.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @NotEmpty
    private volatile BigDecimal balance;

    @NotEmpty
    private String currency;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private List<Transaction> transactions;

    @NotEmpty
    private Long userId;

    @Version
    private Integer versionId;

    @NotEmpty
    private AccountStatus accountStatus;
}
