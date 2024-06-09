package com.yvolabs.securedocs.enumeration;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */

public enum TokenType {
    ACCESS("access-token"),
    REFRESH("refresh-token");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
