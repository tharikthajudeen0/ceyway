package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.service.GoogleMapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/googlemaps")
public class GoogleMapsController {

    private final GoogleMapsService googleMapsService;

    @Autowired
    public GoogleMapsController(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @PostMapping("/route")
    public String getRoute(@RequestBody List<String> locations) {
        return googleMapsService.getRoute(locations);
    }
}
