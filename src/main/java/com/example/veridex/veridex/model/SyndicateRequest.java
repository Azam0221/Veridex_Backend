package com.example.veridex.veridex.model;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SyndicateRequest {

    private String bankName;
    private BigDecimal participationAmount;

    private String role;

}

