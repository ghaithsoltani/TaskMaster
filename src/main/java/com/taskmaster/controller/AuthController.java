package com.taskmaster.controller;

import com.taskmaster.dto.AuthResponse;
import com.taskmaster.dto.LoginRequest;
import com.taskmaster.dto.UserRequest;
import com.taskmaster.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller.
 * Handles user registration and login.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}