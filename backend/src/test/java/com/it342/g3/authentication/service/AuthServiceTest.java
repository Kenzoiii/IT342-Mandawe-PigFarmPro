package com.it342.g3.authentication.service;

import com.it342.g3.authentication.dto.AuthResponse;
import com.it342.g3.authentication.dto.RegisterRequest;
import com.it342.g3.authentication.dto.LoginRequest;
import com.it342.g3.authentication.model.User;
import com.it342.g3.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthService
 * Tests the core authentication business logic
 */
@DisplayName("Authentication Service Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenBlacklist tokenBlacklist;

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

        testUser = new User(
            "testuser",
            "test@example.com",
            "$2a$10$hashedpassword",
            "Test User"
        );
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Should successfully register a new user with valid data")
    void testRegisterUserSuccess() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token-123");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");

        // Act
        AuthResponse response = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400, response.getExpiresIn());
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenProvider, times(1)).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterUserDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegisterUserDuplicateUsername() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("Username already taken"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with null username")
    void testRegisterUserNullUsername() {
        // Arrange
        registerRequest.setUsername(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("required"));
    }

    @Test
    @DisplayName("Should reject registration with short username")
    void testRegisterUserShortUsername() {
        // Arrange
        registerRequest.setUsername("ab");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("between 3 and 50"));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void testRegisterUserInvalidEmail() {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should reject registration with weak password")
    void testRegisterUserWeakPassword() {
        // Arrange
        registerRequest.setPassword("short");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("at least 8 characters"));
    }

    @Test
    @DisplayName("Should successfully authenticate user with correct credentials")
    void testAuthenticateUserSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token-456");

        // Act
        AuthResponse response = authService.authenticateUser("test@example.com", "password123");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token-456", response.getToken());
        
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedpassword");
    }

    @Test
    @DisplayName("Should reject authentication with non-existent email")
    void testAuthenticateUserNonExistentEmail() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser("nonexistent@example.com", "password123");
        });
        
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Should reject authentication with wrong password")
    void testAuthenticateUserWrongPassword() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser("test@example.com", "wrongpassword");
        });
        
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Should reject authentication with null email")
    void testAuthenticateUserNullEmail() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser(null, "password123");
        });
        
        assertTrue(exception.getMessage().contains("Email is required"));
    }

    @Test
    @DisplayName("Should reject authentication with null password")
    void testAuthenticateUserNullPassword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser("test@example.com", null);
        });
        
        assertTrue(exception.getMessage().contains("Password is required"));
    }

    @Test
    @DisplayName("Should successfully logout user by blacklisting token")
    void testLogoutUserSuccess() {
        // Arrange
        String validToken = "valid-jwt-token";
        when(tokenProvider.validateToken(validToken)).thenReturn(true);

        // Act
        authService.logoutUser(validToken);

        // Assert
        verify(tokenBlacklist, times(1)).blacklistToken(validToken);
    }

    @Test
    @DisplayName("Should not blacklist invalid token during logout")
    void testLogoutUserInvalidToken() {
        // Arrange
        String invalidToken = "invalid-jwt-token";
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        authService.logoutUser(invalidToken);

        // Assert
        verify(tokenBlacklist, never()).blacklistToken(invalidToken);
    }
}
