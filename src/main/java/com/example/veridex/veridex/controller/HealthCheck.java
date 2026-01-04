package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.Test;
import com.example.veridex.veridex.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class HealthCheck {


    @Autowired
    private TestRepository testRepository;


    @GetMapping("/health")
    public String healthCheck(){
        return "OK";
    }
}
