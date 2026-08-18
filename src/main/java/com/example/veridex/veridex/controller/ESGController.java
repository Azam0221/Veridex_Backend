package com.example.veridex.veridex.controller;



import com.example.veridex.veridex.model.ESGReport;
import com.example.veridex.veridex.service.ESGService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/esg")
@RequiredArgsConstructor
public class ESGController {

    private final ESGService esgService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ESGReport> uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("loanId") Long loanId
    ) {
        ESGReport report = esgService.processAndSaveEsgReport(file, loanId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/history/{loanId}")
    public ResponseEntity<List<ESGReport>> getLoanUploadHistory(@PathVariable Long loanId) {
        List<ESGReport> history = esgService.getLoanUploadHistory(loanId);
        return ResponseEntity.ok(history);
    }




}
