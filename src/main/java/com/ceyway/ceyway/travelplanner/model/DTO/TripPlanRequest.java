package com.ceyway.ceyway.travelplanner.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripPlanRequest {
    private String start;
    private String destination;
    private String startDate;
    private String endDate;
    private String vehicleType;
    private int numOfMembers;
    private List<String> selectedOnTheWay;
    private List<String> selectedAttractions;
}
