package com.xyzbank.atm.atm_management_service.dao;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public interface AccountDao extends JpaRepository<Account, Long> {

    /**
     * Finds an account by the userId.
     *
     * @param userId the userId to search for.
     * @return the Account object if found, or null if not found.
     */
    Account findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :newBalance, a.versionId = a.versionId + 1 " +
            "WHERE a.accountId = :accountId AND a.versionId = :versionId")
    int updateBalanceWithVersion(Long accountId, BigDecimal newBalance, Integer versionId);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :newBalance, a.versionId = a.versionId + 1, a.remarks = a.remarks " +
            "WHERE a.accountId = :accountId AND a.versionId = :versionId")
    int updateBalanceWithVersionAndRemarks(Long accountId, BigDecimal newBalance, Integer versionId, String remarks);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.AccountStatus = :accountStatus, a.versionId = a.versionId + 1 " +
            "WHERE a.accountId = :accountId AND a.versionId = :versionId")
    int updateAccountStatus(Long accountId, AccountStatus accountStatus, Integer versionId);

}