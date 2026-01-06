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
}
