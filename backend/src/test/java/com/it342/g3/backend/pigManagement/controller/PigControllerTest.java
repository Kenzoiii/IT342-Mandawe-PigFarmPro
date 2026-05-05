package com.it342.g3.backend.pigManagement.controller;

import com.it342.g3.backend.authentication.model.User;
import com.it342.g3.backend.authentication.service.AuthService;
import com.it342.g3.backend.authentication.service.TokenProvider;
import com.it342.g3.backend.penManagement.model.Pen;
import com.it342.g3.backend.penManagement.repository.PenRepository;
import com.it342.g3.backend.pigManagement.model.Pig;
import com.it342.g3.backend.pigManagement.repository.PigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PigController.class)
@AutoConfigureMockMvc(addFilters = false)
class PigControllerTest {

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
    @DisplayName("POST /api/user/pens/{penId}/pigs returns 401 without auth")
    void createPigWithoutAuthReturns401() throws Exception {
        mockMvc.perform(post("/api/user/pens/1/pigs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"breed\":\"Duroc\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/user/pens/{penId}/pigs returns 201 with valid payload")
    void createPigReturns201() throws Exception {
        when(tokenProvider.validateToken("token-for-1")).thenReturn(true);

        User user = new User();
        user.setId(1L);
        user.setUsername("demo");
        when(authService.getProfile(1L)).thenReturn(user);

        Pen pen = new Pen();
        pen.setPenId(5L);
        pen.setCapacity(10);
        pen.setUser(user);
        when(penRepository.findById(5L)).thenReturn(Optional.of(pen));

        when(pigRepository.findByPenPenId(5L)).thenReturn(List.of());
        when(pigRepository.existsByPigIdentifier(anyString())).thenReturn(false);

        Pig saved = new Pig();
        saved.setPigId(7L);
        saved.setPigIdentifier("PIG-0001");
        saved.setPen(pen);
        when(pigRepository.save(any(Pig.class))).thenReturn(saved);

        mockMvc.perform(post("/api/user/pens/5/pigs")
                .header("Authorization", "Bearer token-for-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"breed\":\"Duroc\",\"currentWeight\":120.5,\"weightUnit\":\"kg\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(7));
    }
}
