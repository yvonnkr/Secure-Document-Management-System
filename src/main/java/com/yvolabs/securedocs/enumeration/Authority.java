package com.yvolabs.securedocs.enumeration;

import lombok.Getter;

import static com.yvolabs.securedocs.constant.Constants.*;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 */

@Getter
public enum Authority {
    USER(USER_AUTHORITIES),
    ADMIN(ADMIN_AUTHORITIES),
    SUPER_ADMIN(SUPER_ADMIN_AUTHORITIES),
    MANAGER(MANAGER_AUTHORITIES);

    private final String value;

    Authority(String value) {
        this.value = value;
    }

}
