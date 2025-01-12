package com.xyzbank.atm.atm_management_service;


import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.account.AccountStatus;
import com.xyzbank.atm.atm_management_service.dao.AccountDao;
import com.xyzbank.atm.atm_management_service.dao.UserDao;
import com.xyzbank.atm.atm_management_service.exception.CancelAccountException;
import com.xyzbank.atm.atm_management_service.exception.CreateAccountException;
import com.xyzbank.atm.atm_management_service.exception.InvalidAccountException;
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
import java.time.LocalDateTime;
import java.util.Optional;

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
                .balance(BigDecimal.ONE)
                .currency("USD")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .userId(user.getId())
                .accountStatus(AccountStatus.AVAILABLE)
                .versionId(1)
                .build();
    }

    @Test
    void testGetOrCreateAccount_validInput_accountAlreadyExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(account);
        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");
        Account result = accountCrudService.getOrCreateAccount(requestModel);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getAccountId());
        assertEquals(accountId, result.getAccountId());
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(accountDao, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    void testGetOrCreateAccount_validInput_userAndAccountNotExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(null);
        when(userDao.saveAndFlush(any(User.class))).thenReturn(user);
        when(accountDao.saveAndFlush(any(Account.class))).thenReturn(account);

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");
        Account result = accountCrudService.getOrCreateAccount(requestModel);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getAccountId());
        assertEquals(accountId, result.getAccountId());
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(1)).saveAndFlush(any(Account.class));
    }

    @Test
    void testGetOrCreateAccount_validInput_accountNotExists() throws Exception {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(userDao.saveAndFlush(any(User.class))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(null);
        when(accountDao.saveAndFlush(any(Account.class))).thenReturn(account);

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");
        Account result = accountCrudService.getOrCreateAccount(requestModel);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertNotNull(result.getAccountId());
        assertEquals(accountId, result.getAccountId());
        verify(accountDao, times(1)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(1)).saveAndFlush(any(Account.class));
    }

    @Test
    void testGetOrCreateAccount_inValidName_exception() throws Exception {
        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("", "username", "password");
        assertThrows(CreateAccountException.class, () -> accountCrudService.getOrCreateAccount(requestModel));

        verify(accountDao, times(0)).findByUserId(eq(user.getId()));
        verify(accountDao, times(0)).saveAndFlush(any(Account.class));
    }

    /*@Test
    void testCreateAccount_noUserSet() {
        Account invalidAccount = new Account(); // No user set
        assertThrows(CreateAccountException.class, () -> accountCrudService.getOrCreateAccount(invalidAccount));
    }*/

    @Test
    void testGetOrCreateAccount_nullInput() {
        assertThrows(CreateAccountException.class, () -> accountCrudService.getOrCreateAccount(null));
    }

    @Test
    void testGetOrCreateAccount_saveUserFailed() {
        when(userDao.findByName(eq(tempName))).thenReturn(null);
        when(userDao.saveAndFlush(any(User.class))).thenThrow(new RuntimeException());

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");

        assertThrows(CreateAccountException.class, () -> accountCrudService.getOrCreateAccount(requestModel));
        verify(accountDao, times(0)).findByUserId(eq(user.getId()));
        verify(userDao, times(1)).findByName(any(String.class));
        verify(userDao, times(1)).saveAndFlush(any(User.class));
        verify(accountDao, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    void testGetOrCreateAccount_saveAccountFailed() {
        when(userDao.findByName(eq(tempName))).thenReturn(user);
        when(userDao.saveAndFlush(any(User.class))).thenReturn(user);
        when(accountDao.findByUserId(eq(user.getId()))).thenReturn(null);
        when(accountDao.saveAndFlush(any(Account.class))).thenThrow(new RuntimeException());

        CreateAccountRequestModel requestModel = new CreateAccountRequestModel("tempName", "username", "password");

        assertThrows(CreateAccountException.class, () -> accountCrudService.getOrCreateAccount(requestModel));
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

    @Test
    void testGetUser_WhenAccountNotFound() {
        when(accountDao.findById(accountId)).thenReturn(Optional.empty());
        Optional<User> user = accountCrudService.getUser(accountId);
        assertTrue(user.isEmpty());
        verify(accountDao, times(1)).findById(accountId);
        verifyNoInteractions(userDao);
    }

    @Test
    void testGetUser_WhenAccountFoundButUserNotFound() {
        when(accountDao.findById(accountId)).thenReturn(Optional.of(account));
        when(userDao.findById(account.getUserId())).thenReturn(Optional.empty());

        Optional<User> user = accountCrudService.getUser(accountId);

        assertTrue(user.isEmpty());
        verify(accountDao, times(1)).findById(accountId);
        verify(userDao, times(1)).findById(account.getUserId());
    }

    @Test
    void testGetUser_WhenAccountAndUserFound() {
        when(accountDao.findById(accountId)).thenReturn(Optional.of(account));
        when(userDao.findById(account.getUserId())).thenReturn(Optional.of(user));

        Optional<User> user = accountCrudService.getUser(accountId);

        assertTrue(user.isPresent());
        verify(accountDao, times(1)).findById(accountId);
        verify(userDao, times(1)).findById(account.getUserId());
    }

    @Test
    void testGetBalance_InvalidAccountRequestModel() {
        AccountRequestModel requestModel = new AccountRequestModel("");

        assertThrows(InvalidAccountException.class, () -> accountCrudService.getBalance(requestModel));
        verifyNoInteractions(accountDao);
    }

    @Test
    void testGetBalance_AccountDoesNotExist() {
        AccountRequestModel requestModel = new AccountRequestModel(tempName);

        assertThrows(InvalidAccountException.class, () -> accountCrudService.getBalance(requestModel));
    }

    @Test
    void testGetBalance_AccountExists() throws InvalidAccountException {
        when(accountDao.findByUserId(user.getId())).thenReturn(account);
        when(userDao.findByName(tempName)).thenReturn(user);

        AccountRequestModel requestModel = new AccountRequestModel(tempName);
        BigDecimal balance = accountCrudService.getBalance(requestModel);

        assertNotNull(balance);
        assertEquals(BigDecimal.ONE, balance);
    }
}