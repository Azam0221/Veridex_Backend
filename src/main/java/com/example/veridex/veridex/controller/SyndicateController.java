package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.SyndicateDashboardDTO;
import com.example.veridex.veridex.model.SyndicateMember;
import com.example.veridex.veridex.model.SyndicateRequest;
import com.example.veridex.veridex.model.VerificationReport;
import com.example.veridex.veridex.service.SyndicateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/syndicate")
@RequiredArgsConstructor
public class SyndicateController {

    private final SyndicateService syndicateService;

    @PostMapping("/add/{loanId}")
    public ResponseEntity<SyndicateMember> addMember(
            @PathVariable Long loanId,
            @RequestBody SyndicateRequest request) {
        return syndicateService.addMember(loanId, request);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<SyndicateDashboardDTO>> getMyPortfolio(@RequestParam String email) {
        return ResponseEntity.ok(syndicateService.getLenderPortfolio(email));
    }


    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<SyndicateMember>> getLoanMembers(@PathVariable Long loanId) {
        return ResponseEntity.ok(syndicateService.getLoanMembers(loanId));
    }


    @GetMapping("/audit-trail/{loanId}")
    public ResponseEntity<List<VerificationReport>> getAuditTrail(@PathVariable Long loanId) {

        return ResponseEntity.ok(syndicateService.getAuditTrail(loanId));
    }

}
