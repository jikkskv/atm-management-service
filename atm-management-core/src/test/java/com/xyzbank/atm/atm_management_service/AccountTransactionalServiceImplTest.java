package com.xyzbank.atm.atm_management_service;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.exception.DepositOperationException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.exception.WithdrawOperationException;
import com.xyzbank.atm.atm_management_service.service.impl.AccountTransactionalServiceImpl;
import com.xyzbank.atm.atm_management_service.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTransactionalServiceImplTest {

    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private AccountTransactionalServiceImpl accountTransactionalService;

    private Account fromAccount;

    private Account toAccount;

    private static final String tempName = "tempName";

    private final Long fromAccountId = 111L;
    private final Long toAccountId = 222L;

    private static final User user = User.builder().name(tempName).id(1L).build();

    @BeforeEach
    void setUp() {
        fromAccount = Account.builder()
                .accountId(fromAccountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .versionId(1)
                .build();


        toAccount = Account.builder()
                .accountId(toAccountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .versionId(1)
                .build();
    }

    @Test
    void testDeposit_success() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(accountDao.updateBalanceWithVersion(fromAccountId, amount, 1)).thenReturn(1);

        assertDoesNotThrow(() -> accountTransactionalService.deposit(fromAccountId, amount, "deposit"));
        verify(accountDao, times(1)).updateBalanceWithVersion(eq(fromAccountId), eq(amount), eq(1));
    }

    @Test
    void testDeposit_failedBcozOfStaleUpdate() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(accountDao.updateBalanceWithVersion(fromAccountId, amount, 1)).thenReturn(0);

        assertThrows(DepositOperationException.class, () -> accountTransactionalService.deposit(fromAccountId, amount, "deposit"));
        verify(accountDao, times(1)).updateBalanceWithVersion(eq(fromAccountId), eq(amount), eq(1));
    }

    @Test
    void testDeposit_invalidAccountId() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(123);
        assertThrows(DepositOperationException.class, () -> accountTransactionalService.deposit(fromAccountId, amount, "deposit"));
        verify(accountDao, times(1)).findById(eq(fromAccountId));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testDeposit_negativeAmount() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(-123);

        assertThrows(DepositOperationException.class, () -> accountTransactionalService.deposit(fromAccountId, amount, "deposit"));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testDeposit_lockedAccount() throws InvalidAccountException {
        fromAccount.setAccountStatus(AccountStatus.LOCKED);
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));

        assertThrows(DepositOperationException.class, () -> accountTransactionalService.deposit(fromAccountId, amount, "deposit"));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testWithdraw_success() throws Exception {
        BigDecimal currentBalance = BigDecimal.valueOf(1000);
        fromAccount.setBalance(currentBalance);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.updateBalanceWithVersion(fromAccountId, currentBalance.subtract(amount), 1)).thenReturn(1);

        assertDoesNotThrow(() -> accountTransactionalService.withdraw(fromAccountId, amount, "withdraw"));
        verify(accountDao, times(1)).updateBalanceWithVersion(eq(fromAccountId), eq(currentBalance.subtract(amount)), eq(1));
    }

    @Test
    void testWithdraw_failedBcozOfStaleUpdate() throws Exception {
        BigDecimal currentBalance = BigDecimal.valueOf(1000);
        fromAccount.setBalance(currentBalance);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.updateBalanceWithVersion(fromAccountId, currentBalance.subtract(amount), 1)).thenReturn(0);

        assertThrows(WithdrawOperationException.class, () -> accountTransactionalService.withdraw(fromAccountId, amount, "withdraw"));
        verify(accountDao, times(1)).updateBalanceWithVersion(eq(fromAccountId), eq(currentBalance.subtract(amount)), eq(1));
    }

    @Test
    void testWithdraw_insufficientBalance() throws InvalidAccountException {
        BigDecimal currentBalance = BigDecimal.valueOf(123);
        fromAccount.setBalance(currentBalance);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        BigDecimal amount = BigDecimal.valueOf(124);

        assertThrows(WithdrawOperationException.class, () -> accountTransactionalService.withdraw(fromAccountId, amount, "withdraw"));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testWithdraw_invalidAccountId() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.empty());

        assertThrows(WithdrawOperationException.class, () -> accountTransactionalService.withdraw(fromAccountId, amount, "withdraw"));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testWithdraw_negativeAmount() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(-123);

        assertThrows(WithdrawOperationException.class, () -> accountTransactionalService.withdraw(fromAccountId, amount, "withdraw"));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testWithdraw_lockedAccount() throws InvalidAccountException {
        fromAccount.setAccountStatus(AccountStatus.LOCKED);
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));

        assertThrows(WithdrawOperationException.class, () -> accountTransactionalService.withdraw(fromAccountId, amount, "withdraw"));
        verify(accountDao, times(0)).updateBalanceWithVersion(anyLong(), any(BigDecimal.class), anyInt());
    }
}