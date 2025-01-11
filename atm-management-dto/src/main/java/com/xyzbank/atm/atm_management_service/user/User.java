package com.xyzbank.atm.atm_management_service.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    private Long id;

    private String name;

    private String userName;

    private String password;
}
