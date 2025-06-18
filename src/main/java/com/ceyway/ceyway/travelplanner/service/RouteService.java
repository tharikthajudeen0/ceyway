package com.ceyway.ceyway.travelplanner.service;

import com.ceyway.ceyway.travelplanner.model.Attraction;
import com.ceyway.ceyway.travelplanner.repository.AttractionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    private final RestTemplate restTemplate;
    private final AttractionRepository attractionRepository;
    private final ObjectMapper objectMapper;

    @Value("${openrouteservice.api.key}")
    private String orsApiKey;

    @Autowired
    private GeoLocationService geoLocationService;

    public RouteService(RestTemplate restTemplate, AttractionRepository attractionRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.attractionRepository = attractionRepository;
        this.objectMapper = objectMapper;
    }


    public List<Attraction> getAttractionsByDistrict(String districtName) {
        return attractionRepository.findByDistrict(districtName);
    }

    // New method: get attractions by coordinates (lat, lng)
    public List<Attraction> getAttractionsByCoordinates(double lat, double lng) {
        String district = geoLocationService.getDistrict(lat, lng);
        System.out.println("Detected district: " + district);
        return attractionRepository.findByDistrictIgnoreCase(district);
    }


    public List<Attraction> findNearbyAttractions(double originLat, double originLng, double destLat, double destLng, double maxDistanceKm) throws Exception {
        // Call OpenRouteService directions API
        String url = String.format(
                "https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%f,%f&end=%f,%f",
                orsApiKey, originLng, originLat, destLng, destLat);

        String response = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(response);
        JsonNode coordinates = root.path("features").get(0).path("geometry").path("coordinates");

        List<double[]> routePoints = new ArrayList<>();
        for (JsonNode coord : coordinates) {
            double lng = coord.get(0).asDouble();
            double lat = coord.get(1).asDouble();
            routePoints.add(new double[]{lat, lng});
        }

        // 1. Get all attractions
        List<Attraction> allAttractions = attractionRepository.findAll();
        List<Attraction> nearbyAttractions = new ArrayList<>();

        // 2. Get destination district from nearest attraction (you could also pass it from frontend or DB)
        String destinationDistrict = getClosestDistrict(destLat, destLng, allAttractions);

        for (Attraction attraction : allAttractions) {
            // Exclude attractions in the same district as destination
            if (attraction.getDistrict().equalsIgnoreCase(destinationDistrict)) {
                continue;
            }

            // Check if any route point is close to the attraction
            for (double[] routePoint : routePoints) {
                double distance = haversine(attraction.getLatitude(), attraction.getLongitude(), routePoint[0], routePoint[1]);
                if (distance <= maxDistanceKm) {
                    nearbyAttractions.add(attraction);
                    break;
                }
            }
        }

        return nearbyAttractions;
    }


    // Haversine formula to calculate distance between 2 lat/lon points in KM
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in KM
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String getClosestDistrict(double destLat, double destLng, List<Attraction> attractions) {
        double minDistance = Double.MAX_VALUE;
        String closestDistrict = "";

        for (Attraction attraction : attractions) {
            double dist = haversine(destLat, destLng, attraction.getLatitude(), attraction.getLongitude());
            if (dist < minDistance) {
                minDistance = dist;
                closestDistrict = attraction.getDistrict();
            }
        }

        return closestDistrict;
    }

}

