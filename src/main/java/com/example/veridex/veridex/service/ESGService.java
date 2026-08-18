package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.Status;
import com.example.veridex.veridex.model.ESGReport;
import com.example.veridex.veridex.model.KPI;
import com.example.veridex.veridex.model.Loan;
import com.example.veridex.veridex.repository.ESGReportRepository;
import com.example.veridex.veridex.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ESGService {

    private final PdfService pdfService;
    private final GeminiService geminiService;
    private final LoanRepository loanRepository;
    private final ESGReportRepository esgReportRepository;

    @Transactional
    public ESGReport processAndSaveEsgReport(MultipartFile file, Long loanId) {

        log.info("Starting ESG Report processing for Loan ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        String pdfText = pdfService.extractText(file);

        String kpiPrompt = buildKpiPromptString(loan.getKpi());

        String extractedJson = geminiService.extractData(pdfText, kpiPrompt);

        ESGReport report = new ESGReport();
        report.setLoan(loan);
        report.setReportingPeriod("2025-Q4");
        report.setRawPdfText(pdfText);
        report.setExtractedDataJson(extractedJson);
        report.setStatus(Status.REQUIRES_REVIEW);

        ESGReport savedReport = esgReportRepository.save(report);
        log.info("Successfully processed and saved ESG Report ID: {}", savedReport.getId());

        return savedReport;
    }

    public List<ESGReport> getLoanUploadHistory(Long loanId) {
        return esgReportRepository.findByLoanId(loanId);
    }


    private String buildKpiPromptString(List<KPI> kpis) {
        StringBuilder kpiPrompt = new StringBuilder();
        for (KPI kpi : kpis) {
            kpiPrompt.append("- Key: \"").append(kpi.getName()).append("\"\n");
            kpiPrompt.append("  Search for: ").append(kpi.getName()).append("\n");
            kpiPrompt.append("  Unit: ").append(kpi.getUnit()).append("\n");
        }
        return kpiPrompt.toString();
    }
}