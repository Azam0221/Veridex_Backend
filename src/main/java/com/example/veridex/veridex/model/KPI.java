package com.example.veridex.veridex.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "kpi")
@Data
public class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double baselineValue;
    private Double targetValue;
    private Double marginAdjustment;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

}
