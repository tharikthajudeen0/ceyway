package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.service.GeoLocationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/geo")
public class GeoLocationController {

    private final GeoLocationService geoLocationService;

    public GeoLocationController(GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    @GetMapping("/place-name")
    public Map<String, String> getPlaceName(@RequestParam double lat, @RequestParam double lon) {
        String placeName = geoLocationService.getPlaceName(lat, lon);
        return Map.of("placeName", placeName);
    }

    @GetMapping("/district")
    public Map<String, String> getDistrict(@RequestParam double lat, @RequestParam double lon) {
        String district = geoLocationService.getDistrict(lat, lon);
        return Map.of("district", district);
    }
}
