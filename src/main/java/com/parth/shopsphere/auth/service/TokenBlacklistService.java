package com.parth.shopsphere.auth.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fallback for token blacklisting.
 * Works without Redis for local development.
 * In production, swap this for the Redis-backed implementation.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expirationMs) {
        long expiryTime = System.currentTimeMillis() + expirationMs;
        blacklist.put(token, expiryTime);
        // Lazy cleanup: remove expired entries
        blacklist.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
    }

    public boolean isBlacklisted(String token) {
        Long expiryTime = blacklist.get(token);
        if (expiryTime == null) return false;
        if (expiryTime < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
