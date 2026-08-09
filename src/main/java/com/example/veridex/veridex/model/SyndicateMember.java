package com.example.veridex.veridex.model;

import com.example.veridex.veridex.enum_.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "syndicate_member")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SyndicateMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;
    private BigDecimal participationAmount;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}
 