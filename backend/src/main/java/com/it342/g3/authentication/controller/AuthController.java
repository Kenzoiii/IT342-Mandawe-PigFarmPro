package com.it342.g3.authentication.controller;

import com.it342.g3.authentication.dto.RegisterRequest;
import com.it342.g3.authentication.dto.LoginRequest;
import com.it342.g3.authentication.dto.AuthResponse;
import com.it342.g3.authentication.service.AuthService;
import com.it342.g3.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller - Vertical Slice Implementation
 * 
 * Handles all authentication-related endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Authenticate user and return JWT token
 * - POST /api/auth/logout - Logout user and blacklist token
 * 
 * @author IT342 Group 3
 * @version 2.0 (Vertical Slice Architecture)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     * 
     * Request body:
     * {
     *   "username": "string (3-50 chars, required)",
     *   "email": "string (valid email, required)",
     *   "password": "string (min 8 chars, required)",
     *   "fullName": "string (required)"
     * }
     * 
     * Response: 201 Created on success, 409 Conflict on duplicate, 400 Bad Request on validation error
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true, authResponse, "Registration successful")
            );
        } catch (IllegalArgumentException e) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails("VALID-001", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already")) {
                ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails("DB-002", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ApiResponse<>(false, null, "Duplicate entry", error)
                );
            }
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails("SYSTEM-001", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, null, "Registration failed", error)
            );
        }
    }

    /**
     * Login user with email and password
     * POST /api/auth/login
     * 
     * Request body:
     * {
     *   "email": "string (required)",
     *   "password": "string (required)"
     * }
     * 
     * Response: 200 OK with JWT token on success, 401 Unauthorized on invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(
                new ApiResponse<>(true, authResponse, "Login successful")
            );
        } catch (IllegalArgumentException e) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails("VALID-001", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        } catch (RuntimeException e) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails("AUTH-001", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiResponse<>(false, null, "Authentication failed", error)
            );
        }
    }

    /**
     * Logout user - blacklist their token
     * POST /api/auth/logout
     * 
     * Header: Authorization: Bearer <token>
     * 
     * Response: 200 OK on success
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logoutUser(token);
            }
            return ResponseEntity.ok(
                new ApiResponse<>(true, null, "Logged out successfully")
            );
        } catch (Exception e) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails("SYSTEM-001", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, null, "Logout failed", error)
            );
        }
    }
}
