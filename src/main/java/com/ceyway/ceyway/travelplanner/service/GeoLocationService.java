package com.ceyway.ceyway.travelplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoLocationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String NOMINATIM_URL_TEMPLATE =
            "https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1";

    public GeoLocationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getPlaceName(double lat, double lon) {
        String url = String.format(NOMINATIM_URL_TEMPLATE, lat, lon);
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "CeywayTravelPlanner/1.0"); // Required by Nominatim
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode body = response.getBody();
            if (body != null && body.has("display_name")) {
                String fullName = body.get("display_name").asText();
                return fullName.split(",")[0].trim(); // Just the main place name
            }
            return "Unknown location";
        } catch (Exception e) {
            return "Error fetching location: " + e.getMessage();
        }
    }

    public String getDistrict(double lat, double lon) {
        String url = String.format(NOMINATIM_URL_TEMPLATE, lat, lon);
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "CeywayTravelPlanner/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode address = root.path("address");

            if (address.has("district")) {
                return cleanDistrictName(address.get("district").asText());
            }
            if (address.has("state_district")) {
                return cleanDistrictName(address.get("state_district").asText());
            }
            if (address.has("town")) {
                return cleanDistrictName(address.get("town").asText());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Unknown";
    }

    private String cleanDistrictName(String rawName) {
        return rawName.replace(" District", "").trim();
    }
}
