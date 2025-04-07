package com.ceyway.ceyway.travelplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    private static final double TEMPERATURE = 0.1;
    private static final int MAX_TOKENS = 1000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private String generateLLMPrompt(String start, String destination, String startDate, String endDate,
                                     String vehicleType, int numOfMembers,
                                     List<String> selectedOnTheWay, List<String> selectedAttractions) {

        return String.format("""
            Generate a structured JSON trip plan for a group of %d people traveling from %s to %s 
            using a %s from %s to %s.

            The group wants to visit the following locations on the way: %s.
            At the destination, they want to visit the following attractions: %s.

            Guidelines:
            - The total trip duration should be calculated based on the travel dates.
            - Split the itinerary across the calculated number of days.
            - Plan activities based on realistic travel time and group size.
            - Take into account the vehicle type when estimating travel time.
            - For each day, provide:
                - A list of activities with:
                    - Time (e.g., "9:00 AM")
                    - Place name
                    - Duration at the place (e.g., "1 hour")
                    - Distance and travel time from the previous location (if applicable)

            Output must be a compact structured JSON object in one line.
            Example format:
            {"days": [{"date": "2024-04-07", "activities": [{"time": "9:00 AM", "place": "Attraction A", "duration": "1 hour", "distance": "10 km", "travelTime": "15 min"}]}]}
        """,
                numOfMembers, start, destination, vehicleType, startDate, endDate,
                String.join(", ", selectedOnTheWay),
                String.join(", ", selectedAttractions)
        );
    }

    public JsonNode getTripPlanResponse(String start, String destination, String startDate, String endDate,
                                        String vehicleType, int numOfMembers,
                                        List<String> selectedOnTheWay, List<String> selectedAttractions) {
        try {
            String prompt = generateLLMPrompt(start, destination, startDate, endDate, vehicleType, numOfMembers, selectedOnTheWay, selectedAttractions);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("temperature", TEMPERATURE);
            requestBody.put("max_tokens", MAX_TOKENS);
            requestBody.put("messages", Collections.singletonList(Map.of("role", "user", "content", prompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Error: Unable to generate trip plan, OpenAI returned status " + response.getStatusCode());
            }

            return extractResponseMessage(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error communicating with OpenAI: " + e.getMessage(), e);
        }
    }

    private JsonNode extractResponseMessage(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).path("message").path("content");
                return objectMapper.readTree(messageNode.asText());
            }
            throw new RuntimeException("Error: No choices found in the response.");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage(), e);
        }
    }
}
