package com.it342.g3.backend.controller;

import com.it342.g3.backend.dto.ApiResponse;
import com.it342.g3.backend.dto.UpdatePasswordRequest;
import com.it342.g3.backend.dto.UpdateProfileRequest;
import com.it342.g3.backend.model.User;
import com.it342.g3.backend.repository.UserRepository;
import com.it342.g3.backend.service.AuthService;
import com.it342.g3.backend.service.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Protected endpoint: requires Authorization: Bearer <jwt>
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = tokenProvider.resolveUserIdFromAuthorization(authorization);
        if (userId == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Hide password
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdateProfileRequest request
    ) {
        Long userId = tokenProvider.resolveUserIdFromAuthorization(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        String username = trimToNull(request.getUsername());
        if (username != null && !username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "Username already exists"));
        }

        String email = trimToNull(request.getEmail());
        if (email != null && !email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "Email already exists"));
        }

        if (username != null) {
            user.setUsername(username);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (request.getFullName() != null) {
            user.setFullName(trimToNull(request.getFullName()));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, mapUser(saved), "Profile updated"));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdatePasswordRequest request
    ) {
        Long userId = tokenProvider.resolveUserIdFromAuthorization(authorization);
        if (userId == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        User user = authService.getProfile(userId);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse<>(false, null, "User not found"), HttpStatus.NOT_FOUND);
        }

        String currentPassword = trimToNull(request.getCurrentPassword());
        String newPassword = trimToNull(request.getNewPassword());

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "Current and new password are required"));
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, null, "Current password is incorrect"));
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, "New password must be at least 8 characters"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "Password updated"));
    }

    private Map<String, Object> mapUser(User user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("fullName", user.getFullName());
        payload.put("role", user.getRole());
        payload.put("createdAt", user.getCreatedAt());
        return payload;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
