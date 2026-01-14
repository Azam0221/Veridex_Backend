package com.example.veridex.veridex.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
public class SyndicateDashboardDTO {

    private Long loanId;
    private String bankName;
    private BigDecimal investmentAmount;
 
    private Double ownershipSharePercentage;
    private BigDecimal myShareOfSavings;

    private BigDecimal totalLoanSavings;
    private BigDecimal baseMargin;
    private BigDecimal currentMargin;

    private List<BenchmarkMetric> benchmarks;


    @Data
    @Builder
    public static class BenchmarkMetric {
        private String kpiName;
        private double borrowerValue;
        private double industryAvg;
        private String unit;
        private boolean isBetter;
        private double percentageDiff;
    }
}
