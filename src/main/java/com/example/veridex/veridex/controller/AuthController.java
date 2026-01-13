package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.AuthResponse;
import com.example.veridex.veridex.model.LoginRequest;
import com.example.veridex.veridex.model.RegisterRequest;
import com.example.veridex.veridex.model.User;
import com.example.veridex.veridex.repository.UserRepository;
import com.example.veridex.veridex.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        return authService.loginVerify(loginRequest, response);
    }

    @RequestMapping("register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response){
        return authService.register(registerRequest, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        System.out.println("Logout called");
        return authService.logout(response);

    }

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername();

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(item -> item.getAuthority())
                    .orElse("USER");

            User user = userRepository.findByEmail(email);


            return ResponseEntity.ok(new AuthResponse(
                    200,
                    "User is authenticated",
                    user.getName(),
                    email,
                    role,
                    null,
                    LocalDateTime.now()
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

}
