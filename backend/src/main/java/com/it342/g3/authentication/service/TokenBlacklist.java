package com.it342.g3.authentication.service;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token Blacklist - Maintains a set of blacklisted/invalidated tokens
 * Used during logout to prevent token reuse
 * Vertical Slice: Authentication (Cross-cutting service)
 */
@Component
public class TokenBlacklist {
    
    private final Set<String> blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Blacklist a token
     */
    public void blacklistToken(String token) {
        blacklist.add(token);
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }

    /**
     * Remove token from blacklist (rarely used, mainly for testing)
     */
    public void removeFromBlacklist(String token) {
        blacklist.remove(token);
    }

    /**
     * Clear all blacklisted tokens
     */
    public void clearBlacklist() {
        blacklist.clear();
    }

    /**
     * Get current blacklist size
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }
}
