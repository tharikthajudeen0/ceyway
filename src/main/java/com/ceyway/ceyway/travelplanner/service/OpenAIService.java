package com.ceyway.ceyway.travelplanner.service;

import com.ceyway.ceyway.travelplanner.model.DTO.WeatherResponse;
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
    private final RetrievalService retrievalService;
    private final WeatherService weatherService;


    public OpenAIService(RestTemplate restTemplate, ObjectMapper objectMapper, RetrievalService retrievalService, WeatherService weatherService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.retrievalService = retrievalService;
        this.weatherService = weatherService;
    }

    private String generateLLMPrompt(String start, String destination, String startDate, String endDate,
                                     String vehicleType, int numOfMembers,
                                     List<String> selectedOnTheWay, List<String> selectedAttractions) {

        // Combine keywords to query retrieval service
        String retrievalQuery = String.join(" ",
                List.of(start, destination, String.join(" ", selectedOnTheWay), String.join(" ", selectedAttractions)));

        // Retrieve relevant context from your PostgreSQL knowledge base
        String context = retrievalService.retrieveContext(retrievalQuery);
        if (context == null || context.isBlank()) {
            context = "No additional contextual information available.";
        }


        // Fetch weather data
        WeatherResponse startWeather = weatherService.getWeather(start);
        WeatherResponse destinationWeather = weatherService.getWeather(destination);

        String weatherInfo = String.format("""
        Weather forecast:
        - %s: %s, %.1f°C, Humidity: %.0f%%, Wind Speed: %.1f m/s
        - %s: %s, %.1f°C, Humidity: %.0f%%, Wind Speed: %.1f m/s
        """,
                start,
                startWeather != null ? startWeather.getDescription() : "N/A",
                startWeather != null ? startWeather.getTemperature() : 0.0,
                startWeather != null ? startWeather.getHumidity() : 0.0,
                startWeather != null ? startWeather.getWindSpeed() : 0.0,
                destination,
                destinationWeather != null ? destinationWeather.getDescription() : "N/A",
                destinationWeather != null ? destinationWeather.getTemperature() : 0.0,
                destinationWeather != null ? destinationWeather.getHumidity() : 0.0,
                destinationWeather != null ? destinationWeather.getWindSpeed() : 0.0
        );


        // Construct prompt embedding the retrieved context for RAG
        return String.format("""
            Use the following contextual information and weather forecast to assist in planning the trip:
            %s
            
            %s
            
            Generate a structured JSON trip plan for a group of %d people traveling from %s to %s 
            using a %s from %s to %s.
            
            The group wants to visit the following locations on the way: %s.
            At the destination, they want to visit the following attractions: %s.
            
            Guidelines:
            - The total trip duration should be calculated based on the travel dates.
            - Split the itinerary across the calculated number of days.
            - Label each day explicitly as "Day 1", "Day 2", etc.
            - Consider the weather when planning outdoor activities.
            - Plan activities based on realistic travel time and group size.
            - Take into account the vehicle type when estimating travel time.
            - For each day, provide:
                - Day label (e.g., "Day 1")
                - Date (e.g., "2025-04-10")
                - Expected weather details for the day including:
                    - description
                    - temperature
                - A list of activities with:
                    - Time (e.g., "9:00 AM")
                    - Place name
                    - Duration at the place (e.g., "1 hour")
                    - Distance and travel time from the previous location (if applicable)
            - In addition to the itinerary:
                - Include a 'weatherSummary' section mapping each date to a weather object containing:
                    - location
                    - description
                    - temperature (°C)
                    - humidity (percentage)
                    - windSpeed (m/s)
                - Include an 'estimatedBudget' object with rough estimates in LKR for:
                    - transportation
                    - accommodation
                    - food
                    - activities
                    - total
                - Include an overall 'weatherAssessment' field with a short summary indicating whether the weather conditions are good, moderate, or bad for traveling and outdoor activities during the trip.
            
            Output must be a compact structured JSON object in one line.
            
            Example format:
            {
              "tripDuration": "3 days",
              "estimatedBudget": {
                "transportation": "LKR 15000",
                "accommodation": "LKR 20000",
                "food": "LKR 8000",
                "activities": "LKR 5000",
                "total": "LKR 48000"
              },
              "weatherSummary": {
                "2025-04-10": {
                  "location": "Trincomalee",
                  "description": "overcast clouds",
                  "temperature": 31.26,
                  "humidity": 64.0,
                  "windSpeed": 9.83
                }
              },
              "weatherAssessment": "The weather is generally good for travel with some cloud cover but no rain expected.",
              "itinerary": [
                {
                  "day": "Day 1",
                  "date": "2025-04-10",
                  "weather": {
                    "description": "overcast clouds",
                    "temperature": 31.26
                  },
                  "activities": [
                    {
                      "time": "9:00 AM",
                      "place": "Habarana",
                      "duration": "1 hour",
                      "distance": "45 km",
                      "travelTime": "1 hour"
                    }
                  ]
                }
              ]
            }
            """,
                            context,
                            weatherInfo,
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
