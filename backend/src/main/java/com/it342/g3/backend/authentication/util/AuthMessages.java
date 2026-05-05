package com.it342.g3.backend.authentication.util;

/**
 * Standardized authentication message constants
 * Ensures consistent message formatting across all authentication endpoints
 * Used by both controller and service layers
 */
public class AuthMessages {
    
    // Success Messages
    public static final String REGISTRATION_SUCCESS = "Registration successful";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String USER_PROFILE_RETRIEVED = "User profile retrieved successfully";
    
    // Error Messages - Validation
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String MISSING_USERNAME = "Username is required";
    public static final String MISSING_EMAIL = "Email is required";
    public static final String MISSING_PASSWORD = "Password is required";
    public static final String MISSING_FULL_NAME = "Full name is required";
    
    public static final String INVALID_USERNAME_LENGTH = "Username must be between 3 and 50 characters";
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String INVALID_PASSWORD_LENGTH = "Password must be at least 8 characters";
    public static final String INVALID_PASSWORD_STRENGTH = "Password must contain uppercase, lowercase, numbers, and special characters";
    
    // Error Messages - Database
    public static final String DUPLICATE_EMAIL = "Email already registered";
    public static final String DUPLICATE_USERNAME = "Username already taken";
    public static final String DUPLICATE_ENTRY = "Duplicate entry";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String DATABASE_ERROR = "Database error occurred";
    
    // Error Messages - Authentication
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String UNAUTHORIZED = "Unauthorized";
    
    // Error Messages - System
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String REGISTRATION_FAILED = "Registration failed";
    public static final String LOGIN_FAILED = "Login failed";
    public static final String LOGOUT_FAILED = "Logout failed";
    
    // Error Codes
    public static final String ERROR_CODE_VALIDATION = "VALID-001";
    public static final String ERROR_CODE_MISSING_FIELD = "VALID-002";
    public static final String ERROR_CODE_DUPLICATE = "DB-002";
    public static final String ERROR_CODE_NOT_FOUND = "NOT-FOUND";
    public static final String ERROR_CODE_UNAUTHORIZED = "AUTH-001";
    public static final String ERROR_CODE_SYSTEM = "SYSTEM-001";
    public static final String ERROR_CODE_DATABASE = "DB-001";
}
