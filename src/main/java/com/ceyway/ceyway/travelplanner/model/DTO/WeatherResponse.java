package com.ceyway.ceyway.travelplanner.model.DTO;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private String location;
    private String description;
    private double temperature;
    private double humidity;
    private double windSpeed;
}
