package com.it342.g3.backend.controller;

import com.it342.g3.backend.model.User;
import com.it342.g3.backend.dto.RegisterRequest;
import com.it342.g3.backend.dto.LoginRequest;
import com.it342.g3.backend.dto.AuthResponse;
import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Register a new user
     * POST /api/auth/register
     * As per SDD: username (3-50 chars), email (valid), password (min 8 chars), fullName (optional)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@RequestBody RegisterRequest registerRequest) {

        // Validate required fields
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "VALID-002",
                "Missing required field"
            );
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        }

        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "VALID-002",
                "Missing required field"
            );
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        }

        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "VALID-002",
                "Missing required field"
            );
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        }

        // Validate field lengths and format
        Map<String, String> validationErrors = new HashMap<>();

        if (registerRequest.getUsername().length() < 3 || registerRequest.getUsername().length() > 50) {
            validationErrors.put("username", "Username must be between 3 and 50 characters");
        }

        if (!registerRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            validationErrors.put("email", "Invalid email format");
        }

        if (registerRequest.getPassword().length() < 8) {
            validationErrors.put("password", "Password must be at least 8 characters");
        }

        if (!validationErrors.isEmpty()) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "VALID-001",
                "Validation failed",
                validationErrors
            );
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "DB-002",
                "Duplicate entry"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiResponse<>(false, null, "Email already registered", error)
            );
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "DB-002",
                "Duplicate entry"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiResponse<>(false, null, "Username already taken", error)
            );
        }

        try {
            // Register user
            AuthResponse authResponse = authService.registerUser(registerRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true, authResponse, "Registration successful")
            );
        } catch (Exception e) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "SYSTEM-001",
                "Internal server error"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, null, "Registration failed", error)
            );
        }
    }

    /**
     * Login user with email and password
     * POST /api/auth/login
     * As per SDD: email, password required
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@RequestBody LoginRequest loginRequest) {

        // Validate required fields
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "VALID-002",
                "Missing required field"
            );
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "VALID-002",
                "Missing required field"
            );
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, null, "Validation failed", error)
            );
        }

        try {
            // Authenticate user
            AuthResponse authResponse = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

            if (authResponse == null) {
                ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                    "AUTH-001",
                    "Invalid email or password"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(false, null, "Authentication failed", error)
                );
            }

            return ResponseEntity.ok(
                new ApiResponse<>(true, authResponse, "Login successful")
            );

        } catch (Exception e) {
            ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
                "SYSTEM-001",
                "Internal server error"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, null, "Login failed", error)
            );
        }
    }

    /**
     * Logout user (invalidate session)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser() {
        return ResponseEntity.ok(
            new ApiResponse<>(true, null, "Logout successful")
        );
    }
}
