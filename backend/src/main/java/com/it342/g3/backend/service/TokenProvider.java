package com.it342.g3.backend.service;

import com.it342.g3.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

    public String generateToken(User user) {
        // Simple placeholder token (Lab 2)
        return "token-for-" + user.getId();
    }

    public boolean validateToken(String token) {
        // Always return true for simplicity in Lab 2
        return true;
    }
}
