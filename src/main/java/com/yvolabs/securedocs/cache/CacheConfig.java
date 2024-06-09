package com.yvolabs.securedocs.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 06/06/2024
 */

@Configuration
public class CacheConfig {

    @Bean(name = {"userLoginCache"})
    public CacheStore<String, Integer> userCache() {
        return new CacheStore<>(900, TimeUnit.SECONDS); // expiry in 15min

    }
}
