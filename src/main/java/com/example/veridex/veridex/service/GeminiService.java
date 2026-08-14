package com.example.veridex.veridex.service;

import com.example.veridex.veridex.model.AIRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int CHUNK_SIZE = 25000;

    public String extractData(String pdfText, String kpiPrompts){

        log.info("Starting Map-Reduce extraction for text length: {}", pdfText.length());

        List<String> chunks = splitTextIntoChunks(pdfText, CHUNK_SIZE);
        log.info("Split document into {} chunks.", chunks.size());

        List<CompletableFuture<String>> futures = chunks.stream()
                .map(chunk -> CompletableFuture.supplyAsync(() -> processSingleChunk(chunk, kpiPrompts)))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<String> chunkResults = futures.stream()
                .map(CompletableFuture::join)
                .toList();


        return mergeJsonResults(chunkResults);

    }

    private String processSingleChunk(String chunk, String kpiPrompts) {

        String fullPrompt = buildPrompt(chunk, kpiPrompts);
        AIRequest.GeminiRequest request = new AIRequest.GeminiRequest(fullPrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AIRequest.GeminiRequest> entity = new HttpEntity<>(request, headers);

        String url = apiUrl + "?key=" + apiKey;
        try {
            ResponseEntity<AIRequest.GeminiResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AIRequest.GeminiResponse.class
            );

            if (response.getBody() != null && !response.getBody().getCandidates().isEmpty()) {
                String raw = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
                return raw.replace("```json", "").replace("```", "").trim();
            }
        } catch (Exception e) {
            log.error("Gemini API call failed for chunk: {}", e.getMessage());
        }
        return "{}";
    }

    private String mergeJsonResults(List<String> jsonResults) {
        try {
            ObjectNode finalJson = objectMapper.createObjectNode();

            for (String json : jsonResults) {
                if (!json.equals("{}")) {
                    JsonNode node = objectMapper.readTree(json);

                    node.fields().forEachRemaining(entry -> {
                        if (!entry.getValue().isNull() && !entry.getValue().asText().equals("null")) {
                            finalJson.set(entry.getKey(), entry.getValue());
                        }
                    });
                }
            }
            return objectMapper.writeValueAsString(finalJson);
        } catch (Exception e) {
            log.error("Failed to merge JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private String buildPrompt(String chunkText,String kpiPrompts){

        return "You are an ESG Data Analyst. Extract specific KPI values from this text.\n\n" +
                "REQUIRED KPIs TO FIND:\n" + kpiPrompts + "\n\n" +
                "INSTRUCTIONS:\n" +
                "1. Return ONLY a valid JSON object. Do not use Markdown formatting (```json).\n" +
                "2. Use the exact keys provided in the KPI list.\n" +
                "3. If a value is not found, return null.\n\n" +
                "DOCUMENT TEXT:\n" + chunkText;
    }


    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }
        return chunks;
    }



    public String analyzeRisk (String pdfText) {
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
