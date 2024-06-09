package com.yvolabs.securedocs.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */
@Getter
@Setter
public class JwtConfiguration {
    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.secret}")
    private String secret;
}
