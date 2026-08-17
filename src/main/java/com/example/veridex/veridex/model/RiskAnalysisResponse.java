package com.example.veridex.veridex.model;


import lombok.Data;
import java.util.List;

@Data
public class RiskAnalysisResponse {
    private String industry;
    private int riskScore;
    private String riskLevel;
    private String recommendedMarginAdjustment;
    private List<String> keyRisks;
    private String summary;
}
