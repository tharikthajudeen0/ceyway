package com.ceyway.ceyway.travelplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
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

    /**
     * Generates an LLM prompt for a trip plan with day-wise time slots, attractions, and nearby places.
     */
    private String generateLLMPrompt(String start, String destination, List<String> selectedOnTheWay, List<String> selectedAttractions) {
        return String.format("""
    Generate a structured JSON trip plan for a journey from %s to %s. 
    The user wants to visit the following places along the way: %s. 
    At the destination, they want to visit the following attractions: %s. 
    
    The plan should strictly follow these locations:
    - Only include the places listed in the 'on the way' list for the journey.
    - Only include the attractions listed in the 'destination' list at the final destination.
    
    The plan must:
    - Provide a simple itinerary, listing each location/attraction, with time slots.
    - Include the estimated time to spend at each location and attraction.
    - Provide the distance between consecutive locations and attractions.
    
    The response should be in structured JSON format, in a single line, without newlines. 
    Example format:
    {"activities": [{"time": "9:00 AM", "place": "Attraction A", "duration": "1 hour"}]}
    """,
                start, destination, String.join(", ", selectedOnTheWay), String.join(", ", selectedAttractions));
    }


    /**
     * Sends the generated travel itinerary prompt to OpenAI and returns the response as a structured JSON.
     */
    public JsonNode getTripPlanResponse(String start, String destination, List<String> selectedOnTheWay, List<String> selectedAttractions) {
        try {
            // Generate the travel prompt
            String prompt = generateLLMPrompt(start, destination, selectedOnTheWay, selectedAttractions);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("temperature", TEMPERATURE);
            requestBody.put("max_tokens", MAX_TOKENS);
            requestBody.put("messages", Collections.singletonList(Map.of("role", "user", "content", prompt)));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make API call
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            // Check for a successful response status code
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Error: Unable to generate trip plan, OpenAI returned status " + response.getStatusCode());
            }

            // Extract and return the assistant's structured JSON response
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
                // Return the structured JSON response directly
                return objectMapper.readTree(messageNode.asText()); // Parse the content as JSON
            }
            throw new RuntimeException("Error: No choices found in the response.");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage(), e);
        }
    }
}
