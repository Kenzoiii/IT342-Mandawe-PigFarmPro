package com.it342.g3.backend.service;

import com.it342.g3.backend.model.User;
import com.it342.g3.backend.dto.RegisterRequest;
import com.it342.g3.backend.dto.AuthResponse;
import com.it342.g3.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    /**
     * Register a new user with full validation per SDD
     */
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // Create user entity from register request
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole("USER"); // Default role

        // Save user to database
        User savedUser = userRepository.save(user);

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
     */
    public AuthResponse authenticateUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            String token = tokenProvider.generateToken(user);
            return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                token
            );
        }
        return null;
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
