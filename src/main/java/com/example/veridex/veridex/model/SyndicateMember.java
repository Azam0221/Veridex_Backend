package com.example.veridex.veridex.model;

import com.example.veridex.veridex.enum_.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity(name = "syndicate_member")
@Data
public class SyndicateMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;
    private BigDecimal participationAmount;
    private Role role;

    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}
 