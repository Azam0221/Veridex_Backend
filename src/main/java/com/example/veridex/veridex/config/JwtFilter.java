package com.example.veridex.veridex.config;


import com.example.veridex.veridex.model.AuthResponse;
import com.example.veridex.veridex.service.JwtService;
import com.example.veridex.veridex.service.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/") ||
                path.startsWith("/api/auth/") ||
                path.equals("/test/health");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Security Checkpoint: Intercepted request for path: {}", request.getServletPath());

        String token = jwtService.extractTokenFromCookie(request, "access_token");
        String email = null;


        if(token != null && !token.isEmpty()){
            try {
                email = jwtService.extractUserName(token);
                log.debug("Email extracted from token: {}", email);
            } catch (Exception e) {
                log.warn("Error extracting email from token: {}", e.getMessage());
                sendError(response, "Invalid token format", HttpStatus.UNAUTHORIZED);
                return;
            }
        }
        else {
            log.warn("Access token missing from cookies");
            sendError(response, "Access token missing from cookies", HttpStatus.UNAUTHORIZED);
            return;
        }

        if(email!=null && SecurityContextHolder.getContext().getAuthentication() == null){
            try {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.validateToken(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authtoken);
                    log.info("Successfully authenticated user: {}", email);
                } else {
                    log.warn("Token validation failed for user: {}", email);
                    sendError(response, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                log.error("Error during authentication: {}", e.getMessage());
                sendError(response, "Authentication failed", HttpStatus.UNAUTHORIZED);
                return;
            }
        }
            filterChain.doFilter(request,response);

    }

    private void sendError(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        AuthResponse error = new AuthResponse(
                status.value(),
                message,
                null,
                null,
                null,
                Map.of("error", message),
                LocalDateTime.now()
        );
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(), error);

    }
}
