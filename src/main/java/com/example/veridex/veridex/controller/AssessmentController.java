package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.RiskAnalysisResponse;
import com.example.veridex.veridex.service.AssessmentService;
import com.example.veridex.veridex.service.GeminiService;
import com.example.veridex.veridex.service.PdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {


    private final AssessmentService assessmentService;

    @PostMapping("/analyze")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<RiskAnalysisResponse> analyzePreLoanRisk(@RequestParam("file") MultipartFile file) {

        RiskAnalysisResponse response = assessmentService.processPreLoanAssessment(file);
        return ResponseEntity.ok(response);

    }
}