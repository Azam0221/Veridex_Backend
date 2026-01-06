package com.example.veridex.veridex.model;


import com.example.veridex.veridex.enum_.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "esg_report")
@Data
public class ESGReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportingPeriod;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT")
    private String rawPdfText;

    @Column(columnDefinition = "TEXT")
    private String extractedDataJson;

    private Status status;

    private LocalDateTime uploadTimestamp;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @PrePersist
    protected void onCreate() {
        uploadTimestamp = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
    }


}
