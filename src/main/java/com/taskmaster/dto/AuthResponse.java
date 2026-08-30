package com.taskmaster.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for authentication response containing JWT token.
 */
@Data
@Builder
public class AuthResponse {

    private String token;
    private String type;
    private String username;
    private String email;
    private String role;
}