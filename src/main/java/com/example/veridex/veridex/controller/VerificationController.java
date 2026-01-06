package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.VerificationReport;
import com.example.veridex.veridex.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/run")
    public ResponseEntity<VerificationReport> runVerification(@RequestParam Long reportId) {
        VerificationReport report = verificationService.verifyReport(reportId);
        return ResponseEntity.ok(report);
    }

}
