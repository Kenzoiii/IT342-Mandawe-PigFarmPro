package com.it342.g3.backend.authentication.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory blacklist to invalidate tokens on logout.
 */
@Component
public class TokenBlacklist {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    /**
     * Blacklist a token. Optionally supports expiry in future enhancements.
     */
    public void blacklist(String token) {
        blacklist.put(token, Instant.now());
    }

    /**
     * Check if token is blacklisted.
     */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }
}
