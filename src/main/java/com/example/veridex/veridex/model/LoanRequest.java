package com.example.veridex.veridex.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LoanRequest {
    private String borrowerName;
    private BigDecimal amount;
    private BigDecimal baseMargin;
    private Integer tenorYears;
    private List<KPIRequest> kpis;
}