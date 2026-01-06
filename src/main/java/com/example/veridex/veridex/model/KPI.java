package com.example.veridex.veridex.model;


import com.example.veridex.veridex.enum_.OptimizationType;
import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "kpi")
@Data
public class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String kpiType;
    private String unit;
    private Double baselineValue;
    private Double targetValue;
    private Double marginAdjustment;

    @Enumerated(EnumType.STRING)
    private OptimizationType optimizationType;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

}
