package com.it342.g3.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Authentication Response DTO
 * Returned after successful registration or login
 * Vertical Slice: Authentication
 */
public class AuthResponse {
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("tokenType")
    private String tokenType;
    
    @JsonProperty("expiresIn")
    private Long expiresIn;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(Long userId, String username, String email, String fullName, String token) {
        this(userId, username, email, fullName, token, "Bearer", 86400L);
    }

    public AuthResponse(Long userId, String username, String email, String fullName, 
                        String token, String tokenType, Long expiresIn) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "AuthResponse{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
