package com.ivf.companion.controller;

import com.ivf.companion.dto.RecommendationRequest;
import com.ivf.companion.dto.RecommendationResponse;
import com.ivf.companion.service.AiEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private AiEngineService aiEngineService;

    @PostMapping("/analyze")
    public ResponseEntity<RecommendationResponse> analyzeClinicalParameters(@Valid @RequestBody RecommendationRequest request) {
        RecommendationResponse response = aiEngineService.generateClinicalRecommendation(request);
        return ResponseEntity.ok(response);
    }
}
