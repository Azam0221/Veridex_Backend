package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.Role;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.TokenRepository;
import com.example.veridex.veridex.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    JwtService jwtService;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public ResponseEntity<AuthResponse> register(RegisterRequest request, HttpServletResponse response){
        String accessToken = "";
        String refreshToken = "";
        String name = "";
        String email = request.getEmail();
        Role userRole;
        String roleString = request.getRole();

        if(userRepo.existsByEmail(request.getEmail())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new AuthResponse(400, "Email already exists", null, null,null,
                            Map.of("email", "This email is already registered"), LocalDateTime.now()));
        }
        else {
            User user = new User();

            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setName(request.getName());
            user.setPassword(encoder.encode(user.getPassword()));




            try {
                if (roleString == null || roleString.isEmpty()) {
                    userRole = Role.BORROWER;
                } else {
                    userRole = Role.valueOf(roleString.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new AuthResponse(400, "Invalid Role Selection", null, null,null,
                                Map.of("role", "Role must be BORROWER, AGENT, or LENDER"), LocalDateTime.now()));
            }
            user.setRole(userRole);

            userRepo.save(user);

            accessToken = jwtService.generateAcesssToken(request.getEmail(),userRole);
            refreshToken = jwtService.generateRefreshToken(request.getEmail(),userRole);

            saveUserToken(user,refreshToken);

            setTokenCookies(response, accessToken, refreshToken);
        }

        return ResponseEntity.ok(new AuthResponse(
                200,
                "User registered successfully",
                request.getName(),
                email,
                userRole.toString(),
                null, // No errors
                LocalDateTime.now()
        ));
    }


    public ResponseEntity<AuthResponse> loginVerify(LoginRequest loginRequest, HttpServletResponse response){
        if(loginRequest.getEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new AuthResponse(400, "Email not found", null, null,null,
                            Map.of("email", "Email is empty"), LocalDateTime.now()));
        }
        User user = userRepo.findByEmail(loginRequest.getEmail());
        if(user == null) {
            System.out.println("User not found in database: " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse(401, "Invalid email or password", null, null,null,
                            Map.of("email", "User not found"), LocalDateTime.now()));
        }



        String accessToken ="";
        String refreshToken ="";
        String name = "";
        String email = loginRequest.getEmail();
        System.out.println("User found: " + user);

        try {

            Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {

                accessToken = jwtService.generateAcesssToken(loginRequest.getEmail(), user.getRole());
                refreshToken = jwtService.generateRefreshToken(loginRequest.getEmail(), user.getRole());

                setTokenCookies(response, accessToken, refreshToken);

                return ResponseEntity.ok(new AuthResponse(
                        200,
                        "User login successfully",
                        user.getName(),
                        email,
                        user.getRole().toString(),
                        null,
                        LocalDateTime.now()
                ));
            }
        }
        catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            System.out.println("Reason: Invalid password or credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse(401, "Invalid email or password", null, null,null,
                            Map.of("email", "Invalid credentials"), LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new AuthResponse(500, "Login error", null, null,null,
                            Map.of("error", e.getMessage()), LocalDateTime.now()));
        }

        System.out.println("Login failed - unknown reason");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new AuthResponse(401, "Login failed", null, null,null,
                        Map.of("email", "Something went wrong try again"), LocalDateTime.now()));
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

        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60);
        response.addCookie(accessTokenCookie);
        response.addHeader("Set-Cookie",
                response.getHeader("Set-Cookie") + "; SameSite=Lax");

        // Refresh Token Cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
        response.addHeader("Set-Cookie",
                response.getHeader("Set-Cookie") + "; SameSite=Lax");
    }


    public  ResponseEntity<AuthResponse> logout(HttpServletResponse response){

        jwtService.clearCookie(response,"access_token");
        jwtService.clearCookie(response,"refresh_token");

        System.out.println("Logout successfull");

        return ResponseEntity.ok(new AuthResponse(
                200,
                "Logout successful",
                null,
                null,
                null,
                Map.of(),
                LocalDateTime.now()
        ));
    }

}
