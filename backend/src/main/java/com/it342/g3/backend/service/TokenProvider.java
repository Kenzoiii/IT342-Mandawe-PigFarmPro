package com.it342.g3.backend.service;

import com.it342.g3.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class TokenProvider {

    private final TokenBlacklist tokenBlacklist;
    private final Key signingKey;
    private final long expirationSeconds;

    public TokenProvider(
            TokenBlacklist tokenBlacklist,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expirationSeconds:86400}") long expirationSeconds
    ) {
        this.tokenBlacklist = tokenBlacklist;
        if (jwtSecret == null || jwtSecret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        return parseClaims(token) != null;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return null;
        }
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Long resolveUserIdFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return getUserIdFromToken(token);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (tokenBlacklist.isBlacklisted(token)) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
