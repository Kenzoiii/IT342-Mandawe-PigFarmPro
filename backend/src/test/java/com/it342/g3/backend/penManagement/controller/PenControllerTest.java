package com.it342.g3.backend.penManagement.controller;

import com.it342.g3.backend.authentication.model.User;
import com.it342.g3.backend.authentication.service.AuthService;
import com.it342.g3.backend.authentication.service.TokenProvider;
import com.it342.g3.backend.penManagement.model.Pen;
import com.it342.g3.backend.penManagement.repository.PenRepository;
import com.it342.g3.backend.pigManagement.repository.PigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PenController.class)
@AutoConfigureMockMvc(addFilters = false)
class PenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PenRepository penRepository;

    @MockBean
    private PigRepository pigRepository;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("POST /api/user/pens returns 401 without auth")
    void createPenWithoutAuthReturns401() throws Exception {
        mockMvc.perform(post("/api/user/pens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"penName\":\"Main\",\"capacity\":10}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/user/pens returns 201 with valid payload")
    void createPenReturns201() throws Exception {
        when(tokenProvider.validateToken("token-for-1")).thenReturn(true);

        User user = new User();
        user.setId(1L);
        user.setUsername("demo");
        when(authService.getProfile(1L)).thenReturn(user);

        when(penRepository.existsByPenIdentifier(anyString())).thenReturn(false);

        Pen saved = new Pen();
        saved.setPenId(10L);
        saved.setPenName("Main");
        saved.setPenIdentifier("PEN-0001");
        saved.setCapacity(10);
        saved.setUser(user);

        when(penRepository.save(any(Pen.class))).thenReturn(saved);

        mockMvc.perform(post("/api/user/pens")
                .header("Authorization", "Bearer token-for-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"penName\":\"Main\",\"capacity\":10}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(10));
    }
}
