package com.RMS_Backend.Restaurant.Management.System.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationDTO {
    private Long id;


    private String address;

    private String city;

    private String state;

    private String country;

    private String postalCode;



}
