package com.yvolabs.securedocs.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @param <K> Key
 * @param <V> Value
 * @author Yvonne N
 * @version 1.0
 * @since 06/06/2024
 */
@Slf4j
public class CacheStore<K, V> {

    private final Cache<K, V> cache;

    public CacheStore(int expiryDuration, TimeUnit timeUnit) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration, timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

    }

    public V get(@NotNull K key) {
        log.info("Retrieving from Cache with key: {}", key.toString());
        return cache.getIfPresent(key);
    }

    public void put(@NotNull K key, @NotNull V value) {
        log.info("Storing record in cache for key: {}", key.toString());
        cache.put(key, value);
    }

    public void evict(@NotNull K key) {
        log.info("Removing record from cache for key: {}", key.toString());
        cache.invalidate(key);
    }
}
