package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.service.OpenAIService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/openai")
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Endpoint to generate a trip plan using OpenAI
     *
     * @param request The input data including start, destination, selected locations, and attractions
     * @return A structured, nested JSON response containing the trip plan
     */
    @PostMapping("/generate-trip-plan")
    public Map<String, Object> generateTripPlan(@RequestBody Map<String, Object> request) {
        String start = (String) request.get("start");
        String destination = (String) request.get("destination");
        List<String> selectedOnTheWay = (List<String>) request.get("selectedOnTheWay");
        List<String> selectedAttractions = (List<String>) request.get("selectedAttractions");

        // Input validation
        if (start == null || start.isEmpty() || destination == null || destination.isEmpty()) {
            return Map.of(
                    "status", "error",
                    "message", "Start and destination must be provided."
            );
        }

        if (selectedOnTheWay == null || selectedAttractions == null) {
            return Map.of(
                    "status", "error",
                    "message", "Selected on-the-way locations and attractions must be provided."
            );
        }

        // Call service to generate the trip plan
        JsonNode tripPlan = openAIService.getTripPlanResponse(start, destination, selectedOnTheWay, selectedAttractions);

        // Return a nested structured response
        return Map.of(
                "status", "success",
                "message", "Trip plan generated successfully",
                "data", Map.of(
                        "start", start,
                        "destination", destination,
                        "selectedOnTheWay", selectedOnTheWay,
                        "selectedAttractions", selectedAttractions,
                        "tripPlan", tripPlan
                )
        );
    }
}
