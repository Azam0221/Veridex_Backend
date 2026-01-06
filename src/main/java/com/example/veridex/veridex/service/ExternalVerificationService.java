package com.example.veridex.veridex.service;


import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ExternalVerificationService {

    private final Random random = new Random();

    public double fetchTrustedValue(String kpiType, double reportedValue) {

        double variance = (random.nextDouble() * 0.10) - 0.05;

        if (random.nextInt(10) > 8) {
            variance = 0.15;
        }

        double trustedValue = reportedValue * (1.0 + variance);

        return Math.round(trustedValue * 100.0) / 100.0;
    }
}
