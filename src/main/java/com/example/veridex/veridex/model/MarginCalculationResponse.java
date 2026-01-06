package com.example.veridex.veridex.model;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MarginCalculationResponse {

    private BigDecimal oldMargin;
    private BigDecimal newMargin;
    private BigDecimal totalSavings;
    private List<KPIResult> kpiResults;

    @Data
    @AllArgsConstructor
    public static class KPIResult {
        private String name;
        private boolean achieved;
        private Double marginImpact;
    }
}
