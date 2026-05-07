package com.internship.tool.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);
    private final RestTemplate restTemplate;

    @Value("${app.ai-service-url:http://localhost:5000}")
    private String aiServiceUrl;

    public AiServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public AiResponse describe(String text) {
        return callAi("/describe", text);
    }

    public AiResponse categorise(String text) {
        return callAi("/categorise", text);
    }

    public AiResponse recommend(String text) {
        return callAi("/recommend", text);
    }

    public AiResponse query(String question) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("question", question);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/query",
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AiResponse aiRes = new AiResponse();
                aiRes.setData(response.getBody());
                return aiRes;
            }
        } catch (Exception e) {
            logger.error("AI Service call failed to /query: {}", e.getMessage());
        }
        return null;
    }

    private AiResponse callAi(String endpoint, String text) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("text", text);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + endpoint,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AiResponse aiRes = new AiResponse();
                aiRes.setData(response.getBody());
                return aiRes;
            }
        } catch (Exception e) {
            logger.error("AI Service call failed to {}: {}", endpoint, e.getMessage());
        }
        return null;
    }

    @Data
    public static class AiResponse {
        private Map<String, Object> data;
    }
}
