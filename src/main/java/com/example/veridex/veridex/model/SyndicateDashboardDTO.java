package com.example.veridex.veridex.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class SyndicateDashboardDTO {

    private String bankName;
    private BigDecimal investmentAmount;

    private Double ownershipSharePercentage;
    private BigDecimal myShareOfSavings;

    private BigDecimal totalLoanSavings;
}
