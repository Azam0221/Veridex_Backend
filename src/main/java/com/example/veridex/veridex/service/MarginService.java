package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.OptimizationType;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.ESGReportRepository;
import com.example.veridex.veridex.repository.LoanRepository;
import com.example.veridex.veridex.repository.VerificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarginService {

    private final LoanRepository loanRepository;
    private final ESGReportRepository reportRepository;
    private final VerificationRepository verificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public MarginCalculationResponse calculateNewMargin(Long loanId){

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        ESGReport latestReport = reportRepository.findTopByLoanIdOrderByUploadTimestampDesc(loanId)
                .orElseThrow(() -> new RuntimeException("No reports found for this loan"));

        VerificationReport verification = verificationRepository.findByEsgReportId(latestReport.getId());

        if (verification == null) {
            throw new RuntimeException("Latest report has not been verified yet.");
        }

        Map<String , Object> verificationDetails;

        try{
            verificationDetails = objectMapper.readValue(verification.getVerificationDetailsJson(), Map.class);

        }catch (Exception e){
            throw new RuntimeException("Failed to parse verification data");
        }

        BigDecimal currentMargin = loan.getBaseMargin();
        List<MarginCalculationResponse.KPIResult> kpiResults = new ArrayList<>();

        for (KPI kpi : loan.getKpi()) {
            boolean achieved = false;

            Map<String, Object> kpiData = (Map<String, Object>) verificationDetails.get(kpi.getName());

            if (kpiData != null && "VERIFIED".equals(kpiData.get("status"))) {
                Double actualValue = Double.valueOf(kpiData.get("trusted_source").toString());
                Double targetValue = kpi.getTargetValue();

                if (kpi.getOptimizationType() == OptimizationType.LOWER_IS_BETTER) {
                    achieved = actualValue <= targetValue;
                } else {
                    achieved = actualValue >= targetValue;
                }
            }
            if (achieved) {
                currentMargin = currentMargin.subtract(BigDecimal.valueOf(kpi.getMarginAdjustment()));

                kpiResults.add(new MarginCalculationResponse.KPIResult(
                        kpi.getName(), true, -kpi.getMarginAdjustment()
                ));
            } else {
                currentMargin = currentMargin.add(BigDecimal.valueOf(kpi.getMarginAdjustment()));

                kpiResults.add(new MarginCalculationResponse.KPIResult(
                        kpi.getName(), false, kpi.getMarginAdjustment()
                ));
            }
        }

        BigDecimal marginDiff = loan.getBaseMargin().subtract(currentMargin).abs();

        BigDecimal annualSavings = loan.getPrincipalAmount()
                .multiply(marginDiff)
                .divide(BigDecimal.valueOf(100));

        BigDecimal totalSavings = annualSavings.multiply(BigDecimal.valueOf(loan.getTenorYears()));
        loan.setCurrentMargin(currentMargin);
        loanRepository.save(loan);

        MarginCalculationResponse response = new MarginCalculationResponse();
        response.setOldMargin(loan.getBaseMargin());
        response.setNewMargin(currentMargin);
        response.setTotalSavings(totalSavings);
        response.setKpiResults(kpiResults);

        return response;

    }


}
