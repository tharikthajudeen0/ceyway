package com.ceyway.ceyway.travelplanner.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleMapsService {

    public String getRoute(List<String> locations) {
        if (locations == null || locations.size() < 2) {
            return "Error: At least two locations are required (origin and destination).";
        }

        String origin = locations.get(0);
        String destination = locations.get(locations.size() - 1);
        String waypoints = locations.subList(1, locations.size() - 1).stream()
                .collect(Collectors.joining("|"));

        String url = "https://www.google.com/maps/dir/?api=1&origin=" + origin +
                "&destination=" + destination;

        if (!waypoints.isEmpty()) {
            url += "&waypoints=" + waypoints;
        }

        return url;
    }
}
