package com.taskmaster.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler.
 *
 * Centralizes error handling across all controllers.
 * Returns consistent error responses instead of stack traces.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (e.g., @NotBlank, @Size).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors.toString(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle illegal arguments (business logic errors).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business logic error: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad request",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle security exceptions.
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        log.warn("Security violation: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle access denied (Spring Security).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access denied",
                "You don't have permission to access this resource",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle bad credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed",
                "Invalid username or password",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Catch-all for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Standard error response structure.
     */
    public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}
}