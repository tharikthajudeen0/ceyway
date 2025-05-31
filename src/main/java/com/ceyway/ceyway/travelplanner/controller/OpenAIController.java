package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.model.DTO.TripPlanRequest;
import com.ceyway.ceyway.travelplanner.service.OpenAIService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/travel-app")
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/generate-trip-plan")
    public Map<String, Object> generateTripPlan(@RequestBody TripPlanRequest request) {
        if (request.getStart() == null || request.getStart().isEmpty() ||
                request.getDestination() == null || request.getDestination().isEmpty()) {
            return Map.of(
                    "status", "error",
                    "message", "Start and destination must be provided."
            );
        }

        if (request.getSelectedOnTheWay() == null || request.getSelectedAttractions() == null) {
            return Map.of(
                    "status", "error",
                    "message", "Selected on-the-way locations and attractions must be provided."
            );
        }

        JsonNode tripPlan = openAIService.getTripPlanResponse(
                request.getStart(),
                request.getDestination(),
                request.getStartDate(),
                request.getEndDate(),
                request.getVehicleType(),
                request.getNumOfMembers(),
                request.getSelectedOnTheWay(),
                request.getSelectedAttractions()
        );

        return Map.of(
                "status", "success",
                "message", "Trip plan generated successfully",
                "data", Map.of(
                        "start", request.getStart(),
                        "destination", request.getDestination(),
                        "startDate", request.getStartDate(),
                        "endDate", request.getEndDate(),
                        "vehicleType", request.getVehicleType(),
                        "numOfMembers", request.getNumOfMembers(),
                        "selectedOnTheWay", request.getSelectedOnTheWay(),
                        "selectedAttractions", request.getSelectedAttractions(),
                        "tripPlan", tripPlan
                )
        );
    }
}
