package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.Loan;
import com.example.veridex.veridex.model.LoanRequest;
import com.example.veridex.veridex.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/create")
    public ResponseEntity<Loan> createLoan(@RequestBody LoanRequest request , @RequestParam String email) {
        return loanService.createLoan(request,email);
    }


    @GetMapping("/agent/all")
    public ResponseEntity<List<Loan>> getAllLoansForAgent(String email) {
        return ResponseEntity.ok(loanService.getLoansManagedBy(email));
    }

    @GetMapping("/borrower/all")
    public ResponseEntity<List<Loan>> getAllLoansForBorrower(String email) {
        return ResponseEntity.ok(loanService.getLoansOwnedBy(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }


}
