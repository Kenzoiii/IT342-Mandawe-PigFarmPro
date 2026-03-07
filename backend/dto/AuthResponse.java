package com.it342.g3.backend.dto;

public class AuthResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String token;
    private String tokenType;
    private Long expiresIn;

    public AuthResponse() {
    }

    public AuthResponse(Long userId, String username, String email, String fullName, String token) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = 86400L; // 24 hours in seconds
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
