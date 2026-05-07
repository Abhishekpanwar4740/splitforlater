package com.splitforlater.user.config;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedissonCacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        // Near Cache Configuration for "Hot" Users
        LocalCachedMapOptions<Object, Object> options = LocalCachedMapOptions.defaults()
                // If another instance updates the user, this instance's local cache is invalidated
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LFU)
                .timeToLive(30, TimeUnit.MINUTES)
                .maxIdle(10, TimeUnit.MINUTES);

        // Define the "users" cache with Near Cache enabled
        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
