package com.ivf.companion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private String recommendedProtocol;
    private String successLikelihood;
    private Double confidenceScore;
    private List<String> rationalePoints;
    private List<String> wellnessTips;
    private List<String> recommendedMeds;
}
