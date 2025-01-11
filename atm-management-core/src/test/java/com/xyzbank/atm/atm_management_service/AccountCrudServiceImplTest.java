package com.xyzbank.atm.atm_management_service;


import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.dao.UserDao;
import com.xyzbank.atm.atm_management_service.exception.CancelAccountException;
import com.xyzbank.atm.atm_management_service.exception.CreateAccountException;
import com.xyzbank.atm.atm_management_service.model.AccountRequestModel;
import com.xyzbank.atm.atm_management_service.model.CreateAccountRequestModel;
import com.xyzbank.atm.atm_management_service.service.impl.AccountCrudServiceImpl;
import com.xyzbank.atm.atm_management_service.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountCrudServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private AccountCrudServiceImpl accountCrudService;

    private Account account;

    private static final String tempName = "tempName";

    private static final Long accountId = 111L;

    private static final User user = User.builder().name(tempName).id(1L).build();

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountId(accountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .versionId(1)
                .build();
    }

    @Test
    void testCreateAccount_validInput_accountAlreadyExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(account);
        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");
        Account result = accountCrudService.createAccount(requestModel);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getAccountId());
        assertEquals(accountId, result.getAccountId());
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(accountDao, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    void testCreateAccount_validInput_userAndAccountNotExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(null);
        when(userDao.saveAndFlush(any(User.class))).thenReturn(user);
        when(accountDao.saveAndFlush(any(Account.class))).thenReturn(account);

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");
        Account result = accountCrudService.createAccount(requestModel);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getAccountId());
        assertEquals(accountId, result.getAccountId());
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(1)).saveAndFlush(any(Account.class));
    }

    @Test
    void testCreateAccount_validInput_accountNotExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(userDao.saveAndFlush(any(User.class))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(null);
        when(accountDao.saveAndFlush(any(Account.class))).thenReturn(account);

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");
        Account result = accountCrudService.createAccount(requestModel);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getAccountId());
        assertEquals(accountId, result.getAccountId());
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(1)).saveAndFlush(any(Account.class));
    }

    /*@Test
    void testCreateAccount_noUserSet() {
        Account invalidAccount = new Account(); // No user set
        assertThrows(CreateAccountException.class, () -> accountCrudService.createAccount(invalidAccount));
    }*/

    @Test
    void testCreateAccount_nullInput() {
        assertThrows(CreateAccountException.class, () -> accountCrudService.createAccount(null));
    }

    @Test
    void testCreateAccount_saveUserFailed() {
        when(userDao.findByName(eq(tempName))).thenReturn(null);
        when(userDao.saveAndFlush(any(User.class))).thenThrow(new RuntimeException());

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");

        assertThrows(CreateAccountException.class, () -> accountCrudService.createAccount(requestModel));
        verify(accountDao, times(0)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    void testCreateAccount_saveAccountFailed() {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(userDao.saveAndFlush(any(User.class))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(null);
        when(accountDao.saveAndFlush(any(Account.class))).thenThrow(new RuntimeException());

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");

        assertThrows(CreateAccountException.class, () -> accountCrudService.createAccount(requestModel));
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(1)).saveAndFlush(any(Account.class));
    }

    @Test
    void testCancelAccount_validId() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(account);
        when(accountDao.updateAccountStatus(anyLong(), any(AccountStatus.class), anyInt())).thenReturn(1);

        AccountRequestModel requestModel = new AccountRequestModel("tempName");

        assertEquals(true, accountCrudService.cancelAccount(requestModel));
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(accountDao).updateAccountStatus(anyLong(), any(AccountStatus.class), anyInt());
    }

    @Test
    void testCancelAccount_nullAccountId() throws Exception {
        AccountRequestModel requestModel = new AccountRequestModel(null);
        assertThrows(CancelAccountException.class, () -> accountCrudService.cancelAccount(requestModel));
        verify(accountDao, never()).updateAccountStatus(anyLong(), any(AccountStatus.class), anyInt());
    }

    @Test
    void testCancelAccount_accountNotExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(null);

        AccountRequestModel requestModel = new AccountRequestModel("tempName");

        accountCrudService.cancelAccount(requestModel);
        verify(accountDao, never()).updateAccountStatus(anyLong(), any(AccountStatus.class), anyInt());
    }

    @Test
    void testCancelAccountDeletionFailure() {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(account);
        doThrow(new RuntimeException()).when(accountDao).updateAccountStatus(anyLong(), any(AccountStatus.class), anyInt());

        AccountRequestModel requestModel = new AccountRequestModel("tempName");

        assertThrows(CancelAccountException.class, () -> accountCrudService.cancelAccount(requestModel));
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(accountDao).updateAccountStatus(anyLong(), any(AccountStatus.class), anyInt());
    }
}