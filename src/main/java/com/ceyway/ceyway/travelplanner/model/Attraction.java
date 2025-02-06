package com.ceyway.ceyway.travelplanner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attraction {

    @Id
    private Long id;
    private String name;
    private String category;
    private String description;
    private String district;

}
