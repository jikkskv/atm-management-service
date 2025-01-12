package com.xyzbank.atm.atm_management_service.dao;

import com.xyzbank.atm.atm_management_service.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.name=:name")
    User findByName(@Param("name") String name);
}
