package com.xyzbank.atm.atm_management_service.debt;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class DebtBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long debtId;

    @NotEmpty
    private Long fromAccountId;

    @NotEmpty
    private Long toAccountId;

    @NotEmpty
    private BigDecimal originalAmount;

    @NotEmpty
    private BigDecimal outStandingBalance;

    private LocalDateTime dueDate;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @NotEmpty
    private DebtStatus debtStatus;

    private String remarks;
}
