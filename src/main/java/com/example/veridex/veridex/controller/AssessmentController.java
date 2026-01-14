package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.RiskAnalysisResponse;
import com.example.veridex.veridex.service.GeminiService;
import com.example.veridex.veridex.service.PdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final PdfService pdfService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/analyze")
    public ResponseEntity<RiskAnalysisResponse> analyzePreLoanRisk(@RequestParam("file") MultipartFile file) {
        try {
            String pdfText = pdfService.extractText(file);

            String jsonResponse = geminiService.analyzeRisk(pdfText);

            RiskAnalysisResponse analysis = objectMapper.readValue(jsonResponse, RiskAnalysisResponse.class);
            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            throw new RuntimeException("Analysis failed: " + e.getMessage());
        }
    }
}