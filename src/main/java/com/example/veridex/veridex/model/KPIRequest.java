package com.example.veridex.veridex.model;


import lombok.Data;

@Data
public class KPIRequest {
    private String name;
    private String kpiType;
    private String unit;
    private Double baseline;
    private Double target;
    private Double marginDelta;
    private String optimizationType;
}
