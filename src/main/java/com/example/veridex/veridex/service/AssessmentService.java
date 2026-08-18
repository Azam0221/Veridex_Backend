package com.example.veridex.veridex.service;


import com.example.veridex.veridex.model.RiskAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

    private final PdfService pdfService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskAnalysisResponse processPreLoanAssessment(MultipartFile file) {
        log.info("Starting Pre-Loan Risk Assessment for file: {}", file.getOriginalFilename());

        try {
            String pdfText = pdfService.extractText(file);
            String jsonResponse = geminiService.analyzeRisk(pdfText);
            return objectMapper.readValue(jsonResponse, RiskAnalysisResponse.class);

        } catch (Exception e) {
            log.error("Failed to process risk assessment: {}", e.getMessage());
            throw new RuntimeException("Risk Assessment failed: " + e.getMessage(), e);
        }
    }
}
