package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.AgentDashboardStatsDTO;
import com.example.veridex.veridex.model.Loan;
import com.example.veridex.veridex.model.LoanRequest;
import com.example.veridex.veridex.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/create")
    public ResponseEntity<Loan> createLoan(@RequestBody LoanRequest request , Authentication authentication) {
        String email = authentication.getName();
        Loan createdLoan = loanService.createLoan(request,email);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/agent/stats")
    public ResponseEntity<AgentDashboardStatsDTO> getAgentStats(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(loanService.getAgentStats(email));
    }


    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/agent/all")
    public ResponseEntity<List<Loan>> getAllLoansForAgent(@RequestParam String email){
        return ResponseEntity.ok(loanService.getLoansManagedBy(email));
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/borrower/all")
    public ResponseEntity<List<Loan>> getAllLoansForBorrower(@RequestParam String email) {
        return ResponseEntity.ok(loanService.getLoansOwnedBy(email));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'BORROWER', 'LENDER')")
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    } 


}
