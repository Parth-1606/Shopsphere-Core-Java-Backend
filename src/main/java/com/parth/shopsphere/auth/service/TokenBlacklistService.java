package com.parth.shopsphere.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationMs) {
        redisTemplate.opsForValue().set("jwt_blacklist:" + token, "true", Duration.ofMillis(expirationMs));
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("jwt_blacklist:" + token));
    }
}
