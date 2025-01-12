package com.xyzbank.atm.atm_management_service.debt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @Column(name = "debt_id")
    private Long debtId;

    @NotNull
    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @NotNull
    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @NotNull
    @Column(name = "original_amount", nullable = false, updatable = false)
    private BigDecimal originalAmount;

    @NotNull
    @Column(name = "outstanding_balance", nullable = false)
    private BigDecimal outStandingBalance;

    private LocalDateTime dueDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "debt_status", nullable = false)
    private DebtStatus debtStatus;

    public static DebtBalance buildDebtBalanceObject(Long fromAccountId, Long toAccountId, BigDecimal debtAmount) {
        return DebtBalance.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .originalAmount(debtAmount.negate())
                .outStandingBalance(debtAmount.negate())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .dueDate(LocalDateTime.MAX)
                .debtStatus(DebtStatus.PENDING)
                .build();
    }
}
