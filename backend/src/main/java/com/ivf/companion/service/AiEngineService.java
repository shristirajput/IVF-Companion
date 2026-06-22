package com.ivf.companion.service;

import com.ivf.companion.dto.RecommendationRequest;
import com.ivf.companion.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiEngineService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private RecommendationService fallbackService; // The rule-based engine

    public RecommendationResponse generateClinicalRecommendation(RecommendationRequest request) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY") || apiKey.contains("MOCK")) {
            System.out.println("No valid Gemini API key found. Using rule-based fallback engine.");
            return fallbackService.generateRecommendation(request);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = String.format(
                "You are an expert IVF Clinical AI. Based on the following patient parameters, recommend an IVF protocol.\n" +
                "Age: %d\nAMH: %.2f ng/mL\nFSH: %.2f mIU/mL\nDiagnosis: %s\nPrevious Cycles: %d\n\n" +
                "Respond strictly in this format without markdown wrappers:\n" +
                "PROTOCOL: [Protocol Name]\nCONFIDENCE: [Number 0-100]\nRATIONALE: [Point 1] | [Point 2] | [Point 3]",
                request.getAge(), request.getAmhLevel(), request.getFshLevel(), request.getDiagnosis(), request.getPrevCycles()
            );

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> partsMap = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> textMap = new HashMap<>();
            textMap.put("text", prompt);
            parts.add(textMap);
            partsMap.put("parts", parts);
            contents.add(partsMap);
            requestBody.put("contents", contents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> apiResponse = restTemplate.postForEntity(url, entity, Map.class);
            
            // Extremely basic parsing for demo (Assume standard Gemini response format)
            Map<String, Object> responseBody = apiResponse.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> contentParts = (List<Map<String, Object>>) content.get("parts");
            String aiText = (String) contentParts.get(0).get("text");

            return parseAiText(aiText);

        } catch (Exception e) {
            System.err.println("LLM API Call failed: " + e.getMessage());
            System.out.println("Falling back to rule-based engine.");
            return fallbackService.generateRecommendation(request);
        }
    }

    private RecommendationResponse parseAiText(String text) {
        RecommendationResponse res = new RecommendationResponse();
        res.setRecommendedProtocol("Standard IVF Protocol");
        res.setConfidenceScore(80.0);
        res.setRationalePoints(new ArrayList<>());
        
        try {
            Matcher protoMatcher = Pattern.compile("PROTOCOL:\\s*(.+)").matcher(text);
            if (protoMatcher.find()) res.setRecommendedProtocol(protoMatcher.group(1).trim());

            Matcher confMatcher = Pattern.compile("CONFIDENCE:\\s*(\\d+)").matcher(text);
            if (confMatcher.find()) res.setConfidenceScore(Double.parseDouble(confMatcher.group(1).trim()));

            Matcher ratMatcher = Pattern.compile("RATIONALE:\\s*(.+)").matcher(text);
            if (ratMatcher.find()) {
                String[] points = ratMatcher.group(1).split("\\|");
                List<String> list = new ArrayList<>();
                for (String p : points) list.add(p.trim());
                res.setRationalePoints(list);
            }
        } catch (Exception e) {
            // Ignore parsing errors and return partial
        }
        return res;
    }
}
