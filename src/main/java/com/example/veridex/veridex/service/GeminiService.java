package com.example.veridex.veridex.service;

import com.example.veridex.veridex.model.AIRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractData(String pdfText, String kpiPrompts){

        String fullPrompt = buildPrompt(pdfText, kpiPrompts);

        AIRequest.GeminiRequest request = new AIRequest.GeminiRequest(fullPrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AIRequest.GeminiRequest> entity = new HttpEntity<>(request, headers);

        String url = apiUrl + "?key=" + apiKey;
        ResponseEntity<AIRequest.GeminiResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, AIRequest.GeminiResponse.class
        );
        if (response.getBody() != null && !response.getBody().getCandidates().isEmpty()) {
            return response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
        }
        return "{}";

    }


    private String buildPrompt(String pdfText,String kpiPrompts){

        String truncatedText = pdfText.length() > 30000 ? pdfText.substring(0, 30000) : pdfText;

        return "You are an ESG Data Analyst. Extract specific KPI values from this text.\n\n" +
                "REQUIRED KPIs TO FIND:\n" + kpiPrompts + "\n\n" +
                "INSTRUCTIONS:\n" +
                "1. Return ONLY a valid JSON object. Do not use Markdown formatting (```json).\n" +
                "2. Use the exact keys provided in the KPI list.\n" +
                "3. If a value is not found, return null.\n\n" +
                "DOCUMENT TEXT:\n" + truncatedText;
    }



    public String analyzeRisk(String pdfText) {
        String fullPrompt = buildRiskPrompt(pdfText);

        AIRequest.GeminiRequest request = new AIRequest.GeminiRequest(fullPrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AIRequest.GeminiRequest> entity = new HttpEntity<>(request, headers);

        String url = apiUrl + "?key=" + apiKey;
        ResponseEntity<AIRequest.GeminiResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, AIRequest.GeminiResponse.class
        );

        if (response.getBody() != null && !response.getBody().getCandidates().isEmpty()) {
            String raw = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();

            return raw.replace("```json", "").replace("```", "").trim();
        }
        return "{}";
    }

    private String buildRiskPrompt(String pdfText) {
        String truncatedText = pdfText.length() > 30000 ? pdfText.substring(0, 30000) : pdfText;

        return "You are a Chief Credit Risk Officer at a major bank. Analyze the provided ESG/Sustainability Report to help us decide whether to approve a Sustainability-Linked Loan (SLL).\n\n" +
                "INSTRUCTIONS:\n" +
                "1. Identify the Industry.\n" +
                "2. Assign an ESG Risk Score (0-100, where 100 is excellent/low risk).\n" +
                "3. Determine Risk Level (LOW, MEDIUM, HIGH).\n" +
                "4. Suggest a Margin Adjustment based on risk (e.g., '+0.05%' for high risk, '-0.05%' for excellent).\n" +
                "5. List 3 critical ESG risks found in the text.\n" +
                "6. Provide a 2-sentence executive summary.\n\n" +
                "RETURN ONLY JSON (No Markdown) in this format:\n" +
                "{\n" +
                "  \"industry\": \"String\",\n" +
                "  \"riskScore\": Integer,\n" +
                "  \"riskLevel\": \"String\",\n" +
                "  \"recommendedMarginAdjustment\": \"String\",\n" +
                "  \"keyRisks\": [\"String\", \"String\", \"String\"],\n" +
                "  \"summary\": \"String\"\n" +
                "}\n\n" +
                "DOCUMENT TEXT:\n" + truncatedText;
    } 

}
