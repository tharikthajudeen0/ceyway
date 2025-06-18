package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.model.Attraction;
import com.ceyway.ceyway.travelplanner.service.RouteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/travel-app")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // Endpoint to get attractions based on district name
    @GetMapping("/attractions/{districtName}")
    public List<Attraction> getAttractionsByDistrict(@PathVariable String districtName) {
        List<Attraction> attractions =routeService.getAttractionsByDistrict(districtName);

        if (attractions.isEmpty()) {
            throw new RuntimeException("No attractions found for district: " + districtName);
        }

        return attractions;
    }


    // New endpoint: Get attractions by lat/lng coordinates
    @GetMapping("/attractions/coords")
    public List<Attraction> getAttractionsByCoordinates(
            @RequestParam double lat,
            @RequestParam double lng) {

        List<Attraction> attractions = routeService.getAttractionsByCoordinates(lat, lng);

        if (attractions.isEmpty()) {
            throw new RuntimeException("No attractions found near the provided location.");
        }

        return attractions;
    }


    @GetMapping("/route/nearby-attractions")
    public List<Attraction> getNearbyAttractions(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destLat,
            @RequestParam double destLng,
            @RequestParam(defaultValue = "5") double maxDistanceKm) throws Exception {

        List<Attraction> attractions = routeService.findNearbyAttractions(originLat, originLng, destLat, destLng, maxDistanceKm);

        if (attractions.isEmpty()) {
            throw new RuntimeException("No nearby attractions found within " + maxDistanceKm + " km.");
        }

        return attractions;
    }

}
