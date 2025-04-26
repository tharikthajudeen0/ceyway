package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.model.Attraction;
import com.ceyway.ceyway.travelplanner.model.RouteAttraction;
import com.ceyway.ceyway.travelplanner.service.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/travel-app")
public class TravelController {
    @Autowired
    private TravelService travelService;

    // Endpoint to get attractions based on district name
    @GetMapping("/get-attractions/{districtName}")
    public List<Attraction> getAttractionsByDistrict(@PathVariable String districtName) {
        List<Attraction> attractions = travelService.getAttractionsByDistrict(districtName);

        if (attractions.isEmpty()) {
            throw new RuntimeException("No attractions found for district: " + districtName);
        }

        return attractions;
    }

    @GetMapping("get-ontheway-attractions/{startDistrict}/{endDistrict}")
    public List<RouteAttraction> getRouteAttractionsByStartAndEnd(@PathVariable String startDistrict, @PathVariable String endDistrict) {
        List<RouteAttraction> routeAttractions =  travelService.getRouteAttractionsByDistrict(startDistrict, endDistrict);

        if (routeAttractions.isEmpty()) {
            throw new RuntimeException("No attractions found for between districts");
        }

        return routeAttractions;
    }
}
