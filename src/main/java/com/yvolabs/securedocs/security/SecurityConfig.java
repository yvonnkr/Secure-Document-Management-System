package com.yvolabs.securedocs.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.yvolabs.securedocs.constant.Constants.STRENGTH;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(STRENGTH);
    }
}
