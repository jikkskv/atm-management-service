package com.xyzbank.atm.atm_management_service.service.impl;

import com.xyzbank.atm.atm_management_service.user.User;

import java.util.Optional;

public interface UserService {

    Optional<User> getUser(Long userId);
}
