package com.ivf.companion.dto;

import lombok.Data;

@Data
public class HealthLogRequest {
    private String mood;
    private String symptoms; // e.g., "Bloating, Fatigue"
    private Double hormoneLevel;
    private Double sleepHours;
    private Double weight;
    private String notes;
}
