package com.it342.g3.backend.authentication.controller;

import com.it342.g3.backend.authentication.dto.AuthResponse;
import com.it342.g3.backend.authentication.dto.LoginRequest;
import com.it342.g3.backend.authentication.dto.RegisterRequest;
import com.it342.g3.backend.authentication.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Integration Tests for AuthController
 * Tests the HTTP layer and API endpoint functionality
 * Uses full Spring Boot context for integration testing
 */
@DisplayName("Authentication Controller Integration Tests")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // Rollback after each test for isolation
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
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

        authResponse = new AuthResponse(
            1L,
            "testuser",
            "test@example.com",
            "Test User",
            "jwt-token-123"
        );
    }

    @Test
    @DisplayName("POST /api/auth/register should return 201 Created on successful registration")
    void testRegisterUserReturns201() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 409 Conflict for duplicate email")
    void testRegisterUserDuplicateReturns409() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequest.class)))
            .thenThrow(new IllegalArgumentException("Email already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request for validation error")
    void testRegisterUserValidationReturns400() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequest.class)))
            .thenThrow(new IllegalArgumentException("Username must be between 3 and 50 characters"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 OK with token on successful login")
    void testLoginUserReturns200() throws Exception {
        // Arrange
        when(authService.authenticateUser("test@example.com", "password123")).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 Unauthorized on invalid credentials")
    void testLoginUserInvalidCredentialsReturns401() throws Exception {
        // Arrange
        when(authService.authenticateUser(anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 400 Bad Request for validation error")
    void testLoginUserValidationReturns400() throws Exception {
        // Arrange
        when(authService.authenticateUser(anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Email is required"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/logout should return 200 OK")
    void testLogoutUserReturns200() throws Exception {
        // Arrange
        String token = "jwt-token-123";

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    @DisplayName("POST /api/auth/register should handle null request body")
    void testRegisterUserNullBodyReturns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login should handle null request body")
    void testLoginUserNullBodyReturns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
