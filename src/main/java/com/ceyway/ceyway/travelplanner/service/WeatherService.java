package com.ceyway.ceyway.travelplanner.service;

import com.ceyway.ceyway.travelplanner.model.DTO.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private static final String URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    public WeatherResponse getWeather(String location) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(URL, location, apiKey);

        try {
            // Fetch the response as a Map
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                WeatherResponse weatherResponse = new WeatherResponse();
                weatherResponse.setLocation(location);

                // Extract the weather details
                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
                String weatherDescription = (String) weatherList.get(0).get("description");
                weatherResponse.setDescription(weatherDescription);

                // Extract the main weather details (temperature, humidity, etc.)
                Map<String, Object> main = (Map<String, Object>) response.get("main");
                Object tempObj = main.get("temp");
                double temperature = tempObj instanceof Integer ? ((Integer) tempObj).doubleValue() : (Double) tempObj;
                weatherResponse.setTemperature(temperature);

                Object humidityObj = main.get("humidity");
                double humidity = humidityObj instanceof Integer ? ((Integer) humidityObj).doubleValue() : (Double) humidityObj;
                weatherResponse.setHumidity(humidity);

                // Extract wind details
                Map<String, Object> wind = (Map<String, Object>) response.get("wind");
                Object windSpeedObj = wind.get("speed");
                double windSpeed = windSpeedObj instanceof Integer ? ((Integer) windSpeedObj).doubleValue() : (Double) windSpeedObj;
                weatherResponse.setWindSpeed(windSpeed);

                return weatherResponse;
            } else {
                // Return an empty response if data is not available
                return null;
            }
        } catch (Exception e) {
            // Log or return error details
            return null;
        }
    }
}



