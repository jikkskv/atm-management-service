package com.xyzbank.atm.atm_management_service;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.dao.DebtBalanceDao;
import com.xyzbank.atm.atm_management_service.dao.UserDao;
import com.xyzbank.atm.atm_management_service.debt.DebtBalance;
import com.xyzbank.atm.atm_management_service.exception.DepositOperationException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
import com.xyzbank.atm.atm_management_service.exception.TransferOperationException;
import com.xyzbank.atm.atm_management_service.exception.WithdrawOperationException;
import com.xyzbank.atm.atm_management_service.service.DebtRestructuringService;
import com.xyzbank.atm.atm_management_service.service.impl.AccountTransactionalServiceImpl;
import com.xyzbank.atm.atm_management_service.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTransactionalServiceImplTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private UserDao userDao;

    @Mock
    private DebtBalanceDao debtBalanceDao;

    @Mock
    private DebtRestructuringService debtRestructuringService;

    @Mock
    private AccountTransactionalServiceImpl self;

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
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .versionId(1)
                .build();


        toAccount = Account.builder()
                .accountId(toAccountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .versionId(1)
                .build();
    }

    @Test
    void testDeposit_success() throws InvalidAccountException {
        BigDecimal amount = BigDecimal.valueOf(123);
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(debtBalanceDao.findByFromAccountId(fromAccountId)).thenReturn(Collections.emptyList());
        when(accountDao.updateBalanceWithVersion(fromAccountId, amount, 1)).thenReturn(1);
        doNothing().when(debtRestructuringService).restructureDebt();

        assertDoesNotThrow(() -> accountTransactionalService.deposit(fromAccountId, amount, "deposit"));
        verify(accountDao, times(1)).updateBalanceWithVersion(eq(fromAccountId), eq(amount), eq(1));
    }

    @Test
    void testDeposit_withDebtBalance_transferOperationException() throws InvalidAccountException, TransferOperationException {
        BigDecimal amount = BigDecimal.valueOf(123);
        DebtBalance debtBalance = DebtBalance.buildDebtBalanceObject(fromAccountId, toAccountId, amount);
        when(debtBalanceDao.findByFromAccountId(fromAccountId)).thenReturn(Collections.singletonList(debtBalance));
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(accountDao.findById(eq(toAccountId))).thenReturn(Optional.of(toAccount));
        when(userDao.findById(user.getId())).thenReturn(Optional.of(user));
        when(accountDao.updateBalanceWithVersion(fromAccountId, amount, 1)).thenReturn(1);
        doNothing().when(debtRestructuringService).restructureDebt();

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
        doNothing().when(debtRestructuringService).restructureDebt();
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

    @Test
    void testTransfer_existingOutStandingBalance_success() throws Exception {
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(accountDao.findById(eq(toAccountId))).thenReturn(Optional.of(toAccount));
        BigDecimal transferAmount = BigDecimal.valueOf(123);
        DebtBalance debtBalance = DebtBalance.buildDebtBalanceObject(fromAccountId, toAccountId, transferAmount);
        when(debtBalanceDao.findByFromAccountIdAndToAccountId(toAccountId, fromAccountId)).thenReturn(Collections.singletonList(debtBalance));
        doNothing().when(debtRestructuringService).restructureDebt();

        assertDoesNotThrow(() -> accountTransactionalService.transfer(fromAccountId, toAccountId, transferAmount, "transfer"));
        verify(self, times(0)).performTransferOperationWithTransaction(fromAccount, toAccount,transferAmount,"");
    }

    @Test
    void testTransfer_noOutStandingBalance_success() throws Exception {
        when(accountDao.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(accountDao.findById(eq(toAccountId))).thenReturn(Optional.of(toAccount));
        BigDecimal transferAmount = BigDecimal.valueOf(123);
        when(debtBalanceDao.findByFromAccountIdAndToAccountId(toAccountId, fromAccountId)).thenReturn(Collections.emptyList());
        when(userDao.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(debtRestructuringService).restructureDebt();

        assertDoesNotThrow(() -> accountTransactionalService.transfer(fromAccountId, toAccountId, transferAmount, "transfer"));
        verify(self, times(1)).performTransferOperationWithTransaction(fromAccount, toAccount,transferAmount,"transfer");
    }

    @Test
    void performTransferOperationWithTransaction_successfull() throws TransferOperationException {
        BigDecimal amount = BigDecimal.valueOf(100);
        when(accountDao.updateBalanceWithVersion(eq(fromAccountId), eq(BigDecimal.valueOf(23)), anyInt())).thenReturn(1);
        when(accountDao.updateBalanceWithVersion(eq(toAccountId), eq(amount), anyInt())).thenReturn(1);
        fromAccount.setBalance(BigDecimal.valueOf(123));

        BigDecimal balancedAmount = accountTransactionalService.performTransferOperationWithTransaction(fromAccount, toAccount, amount, "transfer");

        assertEquals(BigDecimal.valueOf(100), balancedAmount);
        verify(accountDao, times(1)).updateBalanceWithVersion(fromAccountId, BigDecimal.valueOf(23), 1);
        verify(accountDao, times(1)).updateBalanceWithVersion(toAccountId, BigDecimal.valueOf(100), 1);
        verify(self).addDebtIfNeeded(fromAccount, toAccount, BigDecimal.valueOf(0));
    }

    @Test
    void performTransferOperationWithTransaction_WithdrawalFails() {
        BigDecimal amount = BigDecimal.valueOf(100);
        when(accountDao.updateBalanceWithVersion(eq(fromAccountId), any(BigDecimal.class), anyInt())).thenReturn(0);

        assertThrows(TransferOperationException.class,
                () -> accountTransactionalService.performTransferOperationWithTransaction(fromAccount, toAccount, amount, "transfer"));
        verify(accountDao, times(1)).updateBalanceWithVersion(fromAccountId, BigDecimal.valueOf(0), 1);
        verify(accountDao, never()).updateBalanceWithVersion(eq(toAccountId), any(BigDecimal.class), anyInt());
    }

    @Test
    void performTransferOperationWithTransaction_DepositFails() {
        BigDecimal amount = BigDecimal.valueOf(100);
        when(accountDao.updateBalanceWithVersion(eq(fromAccountId), eq(BigDecimal.valueOf(23)), anyInt())).thenReturn(1);
        when(accountDao.updateBalanceWithVersion(eq(toAccountId), eq(amount), anyInt())).thenReturn(0);
        fromAccount.setBalance(BigDecimal.valueOf(123));


        assertThrows(TransferOperationException.class,
                () -> accountTransactionalService.performTransferOperationWithTransaction(fromAccount, toAccount, amount, "transfer"));
        verify(accountDao, times(1)).updateBalanceWithVersion(fromAccountId, BigDecimal.valueOf(23), 1);
        verify(accountDao, times(1)).updateBalanceWithVersion(toAccountId, BigDecimal.valueOf(100), 1);

    }
}