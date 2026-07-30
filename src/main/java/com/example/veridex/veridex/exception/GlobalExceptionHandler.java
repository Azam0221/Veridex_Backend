package com.example.veridex.veridex.exception;

import com.example.veridex.veridex.model.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<AuthResponse> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Handled IllegalStateException: {}", ex.getMessage());

        AuthResponse response = new AuthResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null,
                null,
                null,
                Map.of("error", "Registration Failed"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Handled IllegalArgumentException: {}", ex.getMessage());

        AuthResponse response = new AuthResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null,
                null,
                null,
                Map.of("error", "Invalid Input"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Handled BadCredentialsException: {}", ex.getMessage());

        AuthResponse response = new AuthResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password",
                null,
                null,
                null,
                Map.of("error", "Authentication Failed"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        log.error("Unhandled Exception caught: ", ex);

        AuthResponse response = new AuthResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred on the server.",
                null,
                null,
                null,
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}