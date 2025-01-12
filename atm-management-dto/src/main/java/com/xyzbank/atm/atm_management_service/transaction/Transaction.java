package com.xyzbank.atm.atm_management_service.transaction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @NotEmpty
    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @NotEmpty
    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @NotEmpty
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotEmpty
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "remarks", length = 255)
    private String remarks;
}
