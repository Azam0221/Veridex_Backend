package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.Test;
import com.example.veridex.veridex.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class HealthCheck {

    @Autowired
    private TestRepository testRepository;


    @PostMapping("/health")
    public String healthCheck(@RequestBody String name){
        Test test1 = new Test();
        test1.setName(name);
        testRepository.save(test1);

        return "OK";
    }
}
