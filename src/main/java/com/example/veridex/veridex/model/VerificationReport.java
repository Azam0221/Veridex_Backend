package com.example.veridex.veridex.model;


import com.example.veridex.veridex.enum_.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "verification_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Status overallStatus;

    @Column(columnDefinition = "TEXT")
    private String verificationDetailsJson;

    private String auditTrailHash;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifiedAt;

    @OneToOne
    @JoinColumn(name = "report_id")
    private ESGReport esgReport;

    @PrePersist
    protected void onCreate() {
        verifiedAt = LocalDateTime.now();
    }

}
