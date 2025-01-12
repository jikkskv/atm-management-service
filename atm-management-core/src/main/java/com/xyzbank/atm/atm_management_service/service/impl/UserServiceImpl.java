package com.xyzbank.atm.atm_management_service.service.impl;

import com.xyzbank.atm.atm_management_service.dao.UserDao;
import com.xyzbank.atm.atm_management_service.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public Optional<User> getUser(Long userId) {
        if (Objects.isNull(userId)) return Optional.empty();
        return userDao.findById(userId);
    }
}
