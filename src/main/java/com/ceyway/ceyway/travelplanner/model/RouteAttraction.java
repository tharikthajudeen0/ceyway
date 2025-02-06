package com.ceyway.ceyway.travelplanner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteAttraction {

    @Id
    private Long id;

    @Column(name = "start_district")
    private String startDistrict;

    @Column(name = "end_district")
    private String endDistrict;

    private String name;
    private String category;
    private String description;
}
