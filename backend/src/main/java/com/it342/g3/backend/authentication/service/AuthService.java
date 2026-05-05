package com.it342.g3.backend.authentication.service;

import com.it342.g3.backend.authentication.dto.AuthResponse;
import com.it342.g3.backend.authentication.dto.RegisterRequest;
import com.it342.g3.backend.authentication.model.User;
import com.it342.g3.backend.authentication.repository.UserRepository;
import com.it342.g3.backend.authentication.util.AuthMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Authentication Service with enhanced validation and error handling
 * Enforces business rules and validates all inputs before database operations
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    /**
     * Register a new user with comprehensive validation
     * 
     * @param registerRequest Registration details
     * @return AuthResponse with JWT token
     * @throws IllegalArgumentException if validation fails
     */
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // Validate input parameters
        validateRegisterRequest(registerRequest);
        
        // Check for duplicate email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException(AuthMessages.DUPLICATE_EMAIL);
        }
        
        // Check for duplicate username
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException(AuthMessages.DUPLICATE_USERNAME);
        }

        // Create user entity from register request
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole("USER"); // Default role

        // Save user to database
        User savedUser = userRepository.save(user);
        
        // Validate that save operation succeeded
        if (savedUser == null || savedUser.getId() == null) {
            throw new IllegalStateException("Failed to save user to database");
        }

        // Generate token
        String token = tokenProvider.generateToken(savedUser);

        // Return auth response
        return new AuthResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            token
        );
    }

    /**
     * Authenticate user and return token
     * 
     * @param email User's email
     * @param rawPassword User's raw password (not hashed)
     * @return AuthResponse with JWT token
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse authenticateUser(String email, String rawPassword) {
        // Validate inputs
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(AuthMessages.MISSING_EMAIL);
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException(AuthMessages.MISSING_PASSWORD);
        }

        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException(AuthMessages.INVALID_CREDENTIALS));

        // Verify password
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(AuthMessages.INVALID_CREDENTIALS);
        }

        // Generate and return token
        String token = tokenProvider.generateToken(user);
        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            token
        );
    }

    /**
     * Validate registration request data
     * 
     * @param registerRequest Registration details to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegisterRequest(RegisterRequest registerRequest) {
        // Validate username
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException(AuthMessages.MISSING_USERNAME);
        }
        if (registerRequest.getUsername().length() < 3 || registerRequest.getUsername().length() > 50) {
            throw new IllegalArgumentException(AuthMessages.INVALID_USERNAME_LENGTH);
        }

        // Validate email
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(AuthMessages.MISSING_EMAIL);
        }
        if (!isValidEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException(AuthMessages.INVALID_EMAIL_FORMAT);
        }

        // Validate password
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(AuthMessages.MISSING_PASSWORD);
        }
        if (registerRequest.getPassword().length() < 8) {
            throw new IllegalArgumentException(AuthMessages.INVALID_PASSWORD_LENGTH);
        }

        // Validate full name (optional, but if provided must not be empty)
        if (registerRequest.getFullName() != null && registerRequest.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException(AuthMessages.MISSING_FULL_NAME);
        }
    }

    /**
     * Validate email format using regex
     * 
     * @param email Email to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    public String generateToken(User user) {
        return tokenProvider.generateToken(user);
    }

    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getProfile(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
