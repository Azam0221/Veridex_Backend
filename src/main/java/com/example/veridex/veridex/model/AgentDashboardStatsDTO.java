package com.example.veridex.veridex.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AgentDashboardStatsDTO {
    private long totalActiveLoans;
    private long pendingVerification;
    private BigDecimal totalExposure;
    private int avgMarginSavingsBps;
}