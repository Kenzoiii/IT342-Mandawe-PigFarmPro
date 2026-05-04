package com.it342.g3.authentication.service;

import com.it342.g3.authentication.dto.RegisterRequest;
import com.it342.g3.authentication.dto.AuthResponse;
import com.it342.g3.authentication.repository.UserRepository;
import com.it342.g3.authentication.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service - Vertical Slice Implementation
 * 
 * Core business logic for:
 * - User registration with validation
 * - User authentication
 * - Token management
 * - Logout functionality
 * 
 * @author IT342 Group 3
 * @version 2.0 (Vertical Slice Architecture)
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    /**
     * Register a new user with full validation
     * 
     * @param registerRequest containing username, email, password, fullName
     * @return AuthResponse with user info and JWT token
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if user already exists
     */
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // Validate all fields
        validateRegistration(registerRequest);

        // Check for duplicate email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Create user entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole("USER");

        // Save to database
        User savedUser = userRepository.save(user);

        // Generate token
        String token = tokenProvider.generateToken(savedUser);

        // Return response
        return new AuthResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            token,
            "Bearer",
            86400
        );
    }

    /**
     * Authenticate user with email and password
     * 
     * @param email user email
     * @param rawPassword plaintext password
     * @return AuthResponse with token on success
     * @throws RuntimeException if authentication fails
     */
    public AuthResponse authenticateUser(String email, String rawPassword) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate token
        String token = tokenProvider.generateToken(user);

        // Return response
        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            token,
            "Bearer",
            86400
        );
    }

    /**
     * Logout user by blacklisting their token
     * 
     * @param token JWT token to blacklist
     */
    public void logoutUser(String token) {
        if (tokenProvider.validateToken(token)) {
            tokenBlacklist.blacklistToken(token);
        }
    }

    /**
     * Validate registration request
     */
    private void validateRegistration(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
