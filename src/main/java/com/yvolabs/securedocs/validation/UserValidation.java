package com.yvolabs.securedocs.validation;

import com.yvolabs.securedocs.entity.UserEntity;
import com.yvolabs.securedocs.exception.ApiException;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 10/06/2024
 */
public class UserValidation {
    public static void verifyAccountStatus(UserEntity userEntity) {

        if (!userEntity.isEnabled()) {
            throw new ApiException("Account is disabled");
        }
        if (!userEntity.isAccountNonExpired()) {
            throw new ApiException("Account is expired");
        }
        if (!userEntity.isAccountNonLocked()) {
            throw new ApiException("Account is Locked");
        }
    }
}
