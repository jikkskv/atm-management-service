package com.xyzbank.atm.atm_management_service.dao;

import com.xyzbank.atm.atm_management_service.debt.DebtBalance;
import com.xyzbank.atm.atm_management_service.debt.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtBalanceDao extends JpaRepository<DebtBalance, Long> {

    List<DebtBalance> findByDebtStatus(DebtStatus debtStatus);

    List<DebtBalance> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

    List<DebtBalance> findByFromAccountIdAndToAccountId(Long fromAccountId, Long toAccountId);

    List<DebtBalance> findByFromAccountId(Long fromAccountId);
}