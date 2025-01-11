package com.xyzbank.atm.atm_management_service.debt;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DebtBalance {

    private Long debtId;

    private Long fromAccountId;

    private Long toAccountId;

    private BigDecimal originalAmount;

    private BigDecimal outStandingBalance;

    private LocalDateTime dueDate;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private DebtStatus debtStatus;

    private String remarks;
}
