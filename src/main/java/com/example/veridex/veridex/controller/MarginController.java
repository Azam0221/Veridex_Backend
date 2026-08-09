package com.example.veridex.veridex.controller;


import com.example.veridex.veridex.model.MarginCalculationResponse;
import com.example.veridex.veridex.service.MarginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class MarginController {

    private final MarginService marginService;

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/calculate/{loanId}")
    public ResponseEntity<MarginCalculationResponse> calculateMargin(@PathVariable Long loanId) {

        MarginCalculationResponse response = marginService.calculateNewMargin(loanId);
        return ResponseEntity.ok(response);

    }
}
