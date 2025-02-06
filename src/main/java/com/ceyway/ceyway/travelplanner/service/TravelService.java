package com.ceyway.ceyway.travelplanner.service;

import com.ceyway.ceyway.travelplanner.model.Attraction;
import com.ceyway.ceyway.travelplanner.model.RouteAttraction;
import com.ceyway.ceyway.travelplanner.repository.AttractionRepository;
import com.ceyway.ceyway.travelplanner.repository.RouteAttractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private RouteAttractionRepository routeAttractionRepository;

    // Fetch attractions based on district name
    public List<Attraction> getAttractionsByDistrict(String districtName) {
        return attractionRepository.findByDistrict(districtName);
    }

    public List<RouteAttraction> getRouteAttractionsByDistrict(String startDistrict, String endDistrict) {
        return routeAttractionRepository.findByStartDistrictAndEndDistrict(startDistrict, endDistrict);
    }
}
