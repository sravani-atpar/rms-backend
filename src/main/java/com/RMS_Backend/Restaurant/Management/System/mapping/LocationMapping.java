package com.RMS_Backend.Restaurant.Management.System.mapping;

import com.RMS_Backend.Restaurant.Management.System.dto.LocationDTO;
import com.RMS_Backend.Restaurant.Management.System.model.Location;

public interface LocationMapping {

    // Convert entity to DTO
    LocationDTO toDTO(Location location);

    // Convert DTO to new entity instance
    Location toDomain(LocationDTO dto);

    // Update an existing entity with non-null fields from DTO
    void updateDomain(LocationDTO dto, Location target);
}
