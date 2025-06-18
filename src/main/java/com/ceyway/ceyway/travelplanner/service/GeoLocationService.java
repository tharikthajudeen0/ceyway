package com.ceyway.ceyway.travelplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoLocationService {

    private static final String NOMINATIM_URL_TEMPLATE =
            "https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1";

    public String getDistrict(double lat, double lon) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(NOMINATIM_URL_TEMPLATE, lat, lon);

        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode address = root.path("address");

            System.out.println("Raw address: " + address.toString());

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

    // Utility method to clean the district name
    private String cleanDistrictName(String rawName) {
        return rawName.replace(" District", "").trim();
    }
}

