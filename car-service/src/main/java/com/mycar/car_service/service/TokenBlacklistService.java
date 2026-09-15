package com.mycar.car_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public void blacklistToken(String token, long remainingTimeMs) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", remainingTimeMs, TimeUnit.MILLISECONDS);
    }
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}