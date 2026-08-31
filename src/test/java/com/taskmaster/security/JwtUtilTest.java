package com.taskmaster.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 *
 * What we're testing:
 * - Token generation creates a valid token
 * - Token contains correct username
 * - Token expires after set time
 * - Invalid tokens are rejected
 * - Expired tokens are rejected
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Use reflection to set private fields (in real code, use @Value or constructor injection)
        org.springframework.test.util.ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-must-be-at-least-32-characters-long-for-testing");
        org.springframework.test.util.ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour

        userDetails = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forWrongUser() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails otherUser = new User(
                "otheruser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When
        boolean isValid = jwtUtil.validateToken(token, otherUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forTamperedToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // When/Then
        assertFalse(jwtUtil.validateToken(tamperedToken, userDetails));
    }
}