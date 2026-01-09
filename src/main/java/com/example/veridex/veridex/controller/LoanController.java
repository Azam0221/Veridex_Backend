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
    public ResponseEntity<Loan> createLoan(@RequestBody LoanRequest request) {
        Loan newLoan = loanService.createLoan(request);
        return ResponseEntity.ok(newLoan);
    }


    @GetMapping("/all")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }


}
