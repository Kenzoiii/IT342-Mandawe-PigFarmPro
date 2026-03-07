package com.it342.g3.backend.controller;

import com.it342.g3.backend.model.User;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    // Protected endpoint: requires Authorization: Bearer token-for-<userId>
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        String token = authorization.substring("Bearer ".length());
        if (!tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }
        // Simple parse: token-for-<id>
        Long userId = parseUserIdFromToken(token);
        if (userId == null) {
            return new ResponseEntity<>("Invalid token format", HttpStatus.UNAUTHORIZED);
        }
        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Hide password
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }

    private Long parseUserIdFromToken(String token) {
        try {
            String prefix = "token-for-";
            if (!token.startsWith(prefix)) return null;
            String idStr = token.substring(prefix.length());
            return Long.parseLong(idStr);
        } catch (Exception e) {
            return null;
        }
    }
}
