package com.example.veridex.veridex.controller;

import com.example.veridex.veridex.enum_.Status;
import com.example.veridex.veridex.model.ESGReport;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.*;
import com.example.veridex.veridex.service.GeminiService;
import com.example.veridex.veridex.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/esg")
@RequiredArgsConstructor
public class ESGController {

    private final PdfService pdfService;
    private final GeminiService geminiService;
    private final LoanRepository loanRepository;
    private final ESGReportRepository esgReportRepository;

    @PostMapping("/upload")
    public ResponseEntity<ESGReport> uploadReport(
            @RequestParam("file")MultipartFile file,
            @RequestParam("loanId") Long loanId
            ){

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        String pdfText = pdfService.extractText(file);

        StringBuilder kpiPrompt = new StringBuilder();
        for(KPI kpi: loan.getKpi()){
            kpiPrompt.append("- Key: \"").append(kpi.getKpiType()).append("\"\n");
            kpiPrompt.append("  Search for: ").append(kpi.getName()).append("\n");
            kpiPrompt.append("  Unit: ").append(kpi.getUnit()).append("\n");
        }

        String extractedJson = geminiService.extractData(pdfText,kpiPrompt.toString());

        extractedJson = extractedJson.replace("```json", "").replace("```", "").trim();

        ESGReport report = new ESGReport();
        report.setLoan(loan);
        report.setReportingPeriod("2025-Q4");
        report.setRawPdfText(pdfText);
        report.setExtractedDataJson(extractedJson);
        report.setStatus(Status.REQUIRES_REVIEW);

        return ResponseEntity.ok(esgReportRepository.save(report));


    }

}
