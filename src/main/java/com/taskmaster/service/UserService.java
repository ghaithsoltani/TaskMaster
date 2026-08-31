package com.taskmaster.service;

import com.taskmaster.dto.AuthResponse;
import com.taskmaster.dto.LoginRequest;
import com.taskmaster.dto.UserRequest;
import com.taskmaster.entity.User;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(UserRequest request) {
        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getUsername());

        // Generate token
        String token = jwtUtil.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPasswordHash(),
                        java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")
                        )
                )
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtil.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.info("User logged in: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}