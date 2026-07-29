package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.AuthResponse;
import com.example.veridex.veridex.model.LoginRequest;
import com.example.veridex.veridex.model.RegisterRequest;
import com.example.veridex.veridex.model.User;
import com.example.veridex.veridex.repository.UserRepository;
import com.example.veridex.veridex.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    private final UserRepository userRepository;

    @RequestMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        AuthResponse authResponse = authService.loginVerify(loginRequest, response);
        return ResponseEntity.ok(authResponse);
    }

    @RequestMapping("register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response){
        AuthResponse authResponse = authService.register(registerRequest, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        AuthResponse authResponse = authService.logout(response);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername();

            AuthResponse profileResponse = authService.getProfileByEmail(email);
            return ResponseEntity.ok(profileResponse);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

}
