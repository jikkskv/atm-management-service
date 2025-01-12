package com.xyzbank.atm.atm_management_service.utils;

import com.xyzbank.atm.atm_management_service.account.Account;
import com.xyzbank.atm.atm_management_service.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountState {

    private Account account;

    private User user;

}
