package com.yvolabs.securedocs.service;

import com.yvolabs.securedocs.entity.RoleEntity;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

public interface UserService {

    void createUser(String firstName, String lastName, String email, String password);

    RoleEntity getRoleName(String name);

    void verifyAccountKey(String key);
}
