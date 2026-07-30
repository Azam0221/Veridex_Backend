package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.Role;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.TokenRepository;
import com.example.veridex.veridex.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {


    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response){

        log.info("Processing registration for email: {}", request.getEmail());

        if (userRepo.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - Email already exists: {}", request.getEmail());
            throw new IllegalStateException("This email is already registered");
        }

        Role userRole;

        try {
            String roleString = request.getRole();
            userRole = (roleString == null || roleString.isEmpty())
                    ? Role.BORROWER
                    : Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed - Invalid role requested: {}", request.getRole());
            throw new IllegalArgumentException("Role must be BORROWER, AGENT, or LENDER");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);

        userRepo.save(user);
        log.info("User saved to database: {}", user.getEmail());

        String accessToken = jwtService.generateAcesssToken(user.getEmail(), userRole);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), userRole);

        saveUserToken(user, refreshToken);
        setTokenCookies(response, accessToken, refreshToken);

        return new AuthResponse(
                201,
                "User registered successfully",
                request.getName(),
                request.getEmail(),
                userRole.toString(),
                null,
                LocalDateTime.now()
        );
    }


    public AuthResponse loginVerify(LoginRequest loginRequest, HttpServletResponse response){

        log.info("Login request received: {}", loginRequest.getEmail());

        if(loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        try {

            Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {

                User user = userRepo.findByEmail(loginRequest.getEmail());

                String accessToken = jwtService.generateAcesssToken(user.getEmail(), user.getRole());
                String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getRole());

                revokeAllToken(user);
                saveUserToken(user, refreshToken);

                setTokenCookies(response, accessToken, refreshToken);

                log.info("Login successful for user: {}", user.getEmail());

                return new AuthResponse(
                        200,
                        "User login successfully",
                        user.getName(),
                        user.getEmail(),
                        user.getRole().toString(),
                        null,
                        LocalDateTime.now()
                );
            }
        }

        catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {} - Reason: {}", loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }

        throw new RuntimeException("Unexpected login failure");
    }



    public void saveUserToken(User user,String token){
        Token saveToken = new Token();
        saveToken.setUser(user);
        saveToken.setToken(token);
        saveToken.setExpired(false);
        saveToken.setRevoked(false);

        tokenRepository.save(saveToken);
    }

    private void revokeAllToken(User user){
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user);
        validTokens.forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                }
        );

        tokenRepository.saveAll(validTokens);

    }


    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {

        ResponseCookie accessCookie = jwtService.generateCookie("access_token", accessToken, 1000 * 60 * 60L);
        ResponseCookie refreshCookie = jwtService.generateCookie("refresh_token", refreshToken, 1000 * 60 * 60 * 24 * 7L);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public AuthResponse logout(HttpServletResponse response){
        jwtService.clearCookie(response, "access_token");
        jwtService.clearCookie(response, "refresh_token");

        log.info("Logout successful, cookies cleared.");

        return new AuthResponse(
                200,
                "Logout successful",
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public AuthResponse getProfileByEmail(String email) {
        User user = userRepo.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return new AuthResponse(
                200,
                "User is authenticated",
                user.getName(),
                user.getEmail(),
                user.getRole().toString(),
                null,
                LocalDateTime.now()
        );
    }

}
