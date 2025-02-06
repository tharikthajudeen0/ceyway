package com.ceyway.ceyway.travelplanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String getOpenAIResponse(String userMessage) {
        // Use ObjectMapper to create the JSON body dynamically
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("model", "gpt-3.5-turbo");
        requestBodyMap.put("temperature", 0.1);
        requestBodyMap.put("max_tokens", 600);
        requestBodyMap.put("messages", new Object[] {
                new HashMap<String, Object>() {{
                    put("role", "user");
                    put("content", userMessage);
                }}
        });

        try {
            // Convert the map to a JSON string
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            // Set up the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            // Prepare the request entity with the headers and body
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Make the POST request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            // Return the response body
            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
