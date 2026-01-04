package com.example.veridex.veridex.model;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuthResponse {

    private int statusCode;
    private String message;
    private String name;
    private String email;
    private String role;
    private Map<String,String> errors;
    private LocalDateTime timeStamp;

    public AuthResponse(int statusCode, String message, String name, String email,String role, Map<String, String> errors, LocalDateTime timeStamp) {
        this.statusCode = statusCode;
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = role;
        this.errors = errors;
        this.timeStamp = timeStamp;
    }

}
