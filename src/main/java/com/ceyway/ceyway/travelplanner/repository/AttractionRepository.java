package com.ceyway.ceyway.travelplanner.repository;

import com.ceyway.ceyway.travelplanner.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, String> {

    // Find all attractions by district
    List<Attraction> findByDistrict(String district);

}
