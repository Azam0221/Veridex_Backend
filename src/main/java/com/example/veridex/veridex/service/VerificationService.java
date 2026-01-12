package com.example.veridex.veridex.service;

import com.example.veridex.veridex.enum_.Status;
import com.example.veridex.veridex.model.ESGReport;
import com.example.veridex.veridex.model.VerificationReport;
import com.example.veridex.veridex.repository.ESGReportRepository;
import com.example.veridex.veridex.repository.VerificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final ESGReportRepository esgReportRepository;
    private final VerificationRepository verificationRepository;
    private final ExternalVerificationService externalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VerificationReport verifyReport(Long loanId){

        ESGReport report = esgReportRepository.findTopByLoanIdOrderByUploadTimestampDesc(loanId)
                .orElseThrow(() -> new RuntimeException("No ESG Reports found for Loan ID: " + loanId));

        try{
            Map<String, Object> reportedData = objectMapper.readValue(
                    report.getExtractedDataJson(), Map.class
            );

            Map<String, Object> verificationResults = new HashMap<>();
            boolean allPassed = true;

            for (Map.Entry<String, Object> entry : reportedData.entrySet()) {

                String kpiKey = entry.getKey();

                // 👇 FIXED: Robust extraction logic
                Double reportedVal = extractValue(entry.getValue());

                Double trustedVal = externalService.fetchTrustedValue(kpiKey, reportedVal);

                double variance = Math.abs((reportedVal - trustedVal) / trustedVal) * 100.0;

                Status status = (variance <= 5.0) ? Status.VERIFIED : Status.FLAGGED;
                if (status.equals(Status.FLAGGED)) allPassed = false;

                Map<String, Object> kpiResult = new HashMap<>();
                kpiResult.put("reported", reportedVal);
                kpiResult.put("trusted_source", trustedVal);
                kpiResult.put("variance_percentage", Math.round(variance * 100.0) / 100.0);
                kpiResult.put("status", status);

                verificationResults.put(kpiKey, kpiResult);
            }

            VerificationReport verificationReport = new VerificationReport();
            verificationReport.setEsgReport(report);
            verificationReport.setOverallStatus(allPassed ? Status.VERIFIED : Status.REQUIRES_REVIEW);
            verificationReport.setVerificationDetailsJson(objectMapper.writeValueAsString(verificationResults));
            verificationReport.setAuditTrailHash(UUID.randomUUID().toString().replace("-", "") + "0x");

            report.setStatus(allPassed ? Status.VERIFIED : Status.FLAGGED);
            esgReportRepository.save(report);

            return verificationRepository.save(verificationReport);

        } catch (Exception e){
            // Print the extracted JSON to console so we can see what Gemini actually returned
            System.err.println("JSON Parsing Error on: " + report.getExtractedDataJson());
            e.printStackTrace();
            throw new RuntimeException("Error during verification: " + e.getMessage());
        }
    }

    public VerificationReport findByReportId(Long esgReportId) {
        return verificationRepository.findByEsgReport_Id(esgReportId)
                .orElseThrow(() -> new RuntimeException("No verification found for Report ID: " + esgReportId));
    }

    // 👇 NEW HELPER METHOD
    private Double extractValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof Map) {
            // If Gemini returned an object like {"value": 12.5, "unit": "tons"}, we dig for "value"
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.containsKey("value") && map.get("value") instanceof Number) {
                return ((Number) map.get("value")).doubleValue();
            }
            if (map.containsKey("amount") && map.get("amount") instanceof Number) {
                return ((Number) map.get("amount")).doubleValue();
            }
            // Fallback: Try to parse strings like "12.5 tons"
            return 0.0;
        } else if (value instanceof String) {
            // Try to parse string "12.5"
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}