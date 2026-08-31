package com.taskmaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmaster.dto.AuthResponse;
import com.taskmaster.dto.LoginRequest;
import com.taskmaster.dto.UserRequest;
import com.taskmaster.security.JwtFilter;
import com.taskmaster.security.JwtUtil;
import com.taskmaster.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AuthController.
 *
 * @WebMvcTest loads ONLY the web layer (controllers, filters, security).
 * Does NOT load the full application context or database.
 * Fast and focused.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void register_shouldReturn201_withValidRequest() throws Exception {
        // Given
        UserRequest request = UserRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .username("newuser")
                .email("new@example.com")
                .role("USER")
                .build();

        when(userService.register(any(UserRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void register_shouldReturn400_withInvalidRequest() throws Exception {
        // Given - username too short
        UserRequest request = UserRequest.builder()
                .username("ab")  // Less than 3 chars
                .email("invalid-email")  // Invalid format
                .password("123")  // Less than 6 chars
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void login_shouldReturn200_withValidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .username("testuser")
                .role("USER")
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}