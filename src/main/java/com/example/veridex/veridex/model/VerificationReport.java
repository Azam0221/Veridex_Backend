package com.example.veridex.veridex.model;


import com.example.veridex.veridex.enum_.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "verification_report")
@Data
public class VerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Status overallStatus;

    @Column(columnDefinition = "TEXT")
    private String verificationDetailsJson;

    private String auditTrailHash;

    private LocalDateTime verifiedAt;

    @OneToOne
    @JoinColumn(name = "report_id")
    private ESGReport esgReport;

    @PrePersist
    protected void onCreate() {
        verifiedAt = LocalDateTime.now();
    }




}
