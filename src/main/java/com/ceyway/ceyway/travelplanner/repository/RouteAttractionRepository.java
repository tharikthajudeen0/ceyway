package com.ceyway.ceyway.travelplanner.repository;

import com.ceyway.ceyway.travelplanner.model.RouteAttraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteAttractionRepository extends JpaRepository <RouteAttraction, Long> {

    // Find all attractions by start and end district
    List<RouteAttraction> findByStartDistrictAndEndDistrict(String startDistrict, String endDistrict);
}
