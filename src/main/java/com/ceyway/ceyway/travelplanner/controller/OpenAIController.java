package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.model.DTO.TripPlanRequest;
import com.ceyway.ceyway.travelplanner.service.GeoLocationService;
import com.ceyway.ceyway.travelplanner.service.OpenAIService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/travel-app")
public class OpenAIController {

    private final OpenAIService openAIService;
    private final GeoLocationService geoLocationService;

    public OpenAIController(OpenAIService openAIService, GeoLocationService geoLocationService) {
        this.openAIService = openAIService;
        this.geoLocationService = geoLocationService;
    }

    @PostMapping("/generate-trip-plan")
    public Map<String, Object> generateTripPlan(@RequestBody TripPlanRequest request) {
        // Convert coordinates to place names
        String startPlace = geoLocationService.getPlaceName(request.getStartLat(), request.getStartLon());
        String destinationPlace = geoLocationService.getDistrict(request.getDestinationLat(), request.getDestinationLon());

        if (startPlace.equals("Unknown location") || destinationPlace.equals("Unknown location")) {
            return Map.of(
                    "status", "error",
                    "message", "Could not resolve location names from coordinates."
            );
        }

        JsonNode tripPlan = openAIService.getTripPlanResponse(
                startPlace,
                destinationPlace,
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
                        "start", startPlace,
                        "destination", destinationPlace,
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
