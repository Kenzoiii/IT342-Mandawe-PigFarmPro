package com.it342.g3.backend.authentication.service;

import com.it342.g3.backend.authentication.dto.AuthResponse;
import com.it342.g3.backend.authentication.dto.LoginRequest;
import com.it342.g3.backend.authentication.dto.RegisterRequest;
import com.it342.g3.backend.authentication.model.User;
import com.it342.g3.backend.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthService
 * Tests the core authentication business logic with proper validation and error handling
 */
@DisplayName("Authentication Service Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        registerRequest = new RegisterRequest(
            "testuser",
            "test@example.com",
            "password123",
            "Test User"
        );

        loginRequest = new LoginRequest(
            "test@example.com",
            "password123"
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setFullName("Test User");
        testUser.setRole("USER");
    }

    @Test
    @DisplayName("Should successfully register a new user with valid data")
    void testRegisterUserSuccess() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate email")
    void testRegisterUserDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception for duplicate username")
    void testRegisterUserDuplicateUsername() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception for null username")
    void testRegisterUserNullUsername() {
        // Arrange
        registerRequest.setUsername(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid email format")
    void testRegisterUserInvalidEmail() {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception for short password")
    void testRegisterUserShortPassword() {
        // Arrange
        registerRequest.setPassword("short");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
    }

    @Test
    @DisplayName("Should successfully authenticate user with correct credentials")
    void testAuthenticateUserSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(tokenProvider.generateToken(testUser)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.authenticateUser("test@example.com", "password123");

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    @DisplayName("Should throw exception for invalid email during login")
    void testAuthenticateUserInvalidEmail() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser("nonexistent@example.com", "password123");
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid password")
    void testAuthenticateUserInvalidPassword() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser("test@example.com", "wrongpassword");
        });
    }

    @Test
    @DisplayName("Should throw exception for null email during login")
    void testAuthenticateUserNullEmail() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser(null, "password123");
        });
    }

    @Test
    @DisplayName("Should throw exception for null password during login")
    void testAuthenticateUserNullPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser("test@example.com", null);
        });
    }

    @Test
    @DisplayName("Should validate token successfully")
    void testValidateTokenSuccess() {
        // Arrange
        String validToken = "valid-jwt-token";
        when(tokenProvider.validateToken(validToken)).thenReturn(true);

        // Act
        boolean result = authService.validateToken(validToken);

        // Assert
        assertTrue(result);
        verify(tokenProvider, times(1)).validateToken(validToken);
    }

    @Test
    @DisplayName("Should not validate invalid token")
    void testValidateTokenInvalid() {
        // Arrange
        String invalidToken = "invalid-jwt-token";
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        boolean result = authService.validateToken(invalidToken);

        // Assert
        assertFalse(result);
        verify(tokenProvider, times(1)).validateToken(invalidToken);
    }
}
