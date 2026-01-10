package com.example.veridex.veridex.model;


import com.example.veridex.veridex.enum_.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity(name = "loan")
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    private String borrowerName;
    private BigDecimal principalAmount;
    private BigDecimal baseMargin;
    private BigDecimal currentMargin;

    private Integer tenorYears;


    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate nextReportingDate;
    private LocalDate maturityDate;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<KPI> kpi;
}
