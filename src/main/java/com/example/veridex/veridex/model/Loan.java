package com.example.veridex.veridex.model;


import com.example.veridex.veridex.enum_.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity(name = "loan")
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String borrowerName;
    private BigDecimal principalAmount;
    private BigDecimal baseMargin;
    private BigDecimal currentMargin;

    private Integer tenorYears;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<KPI> kpi;
}
