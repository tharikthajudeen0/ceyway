package com.ceyway.ceyway.travelplanner.controller;


import com.ceyway.ceyway.travelplanner.model.DTO.WeatherResponse;
import com.ceyway.ceyway.travelplanner.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/check/{location}")
    public WeatherResponse getWeather(@PathVariable String location) {
        return weatherService.getWeather(location);
    }
}
