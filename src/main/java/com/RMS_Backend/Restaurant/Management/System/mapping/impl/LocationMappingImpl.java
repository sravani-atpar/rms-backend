package com.RMS_Backend.Restaurant.Management.System.mapping.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.LocationDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.LocationMapping;
import com.RMS_Backend.Restaurant.Management.System.model.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMappingImpl implements LocationMapping {

    @Override
    public LocationDTO toDTO(Location location) {
        if (location == null) return null;
        return new LocationDTO(
            location.getId(),

            location.getAddress(),
            location.getCity(),
            location.getState(),
            location.getCountry(),
            location.getPostalCode()
        );
    }

    @Override
    public Location toDomain(LocationDTO dto) {
        if (dto == null) return null;
        Location location = new Location();
        // Note: id is generated, but if provided in DTO we set it (useful for updates/merges)
        location.setId(dto.getId());

        location.setAddress(dto.getAddress());
        location.setCity(dto.getCity());
        location.setState(dto.getState());
        location.setCountry(dto.getCountry());
        location.setPostalCode(dto.getPostalCode());
        return location;
    }

    @Override
    public void updateDomain(LocationDTO dto, Location target) {
        if (dto == null || target == null) return;
        // Do not touch id here; JPA manages it

        if (dto.getAddress() != null) target.setAddress(dto.getAddress());
        if (dto.getCity() != null) target.setCity(dto.getCity());
        if (dto.getState() != null) target.setState(dto.getState());
        if (dto.getCountry() != null) target.setCountry(dto.getCountry());
        if (dto.getPostalCode() != null) target.setPostalCode(dto.getPostalCode());
    }
}