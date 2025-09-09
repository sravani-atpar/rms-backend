package com.RMS_Backend.Restaurant.Management.System.service;

import com.RMS_Backend.Restaurant.Management.System.dto.LocationDTO;

import java.util.List;

public interface LocationService {

    LocationDTO create(LocationDTO dto);

    LocationDTO update(Long id, LocationDTO dto);

    void delete(Long id);

    LocationDTO getById(Long id);

    List<LocationDTO> getAll();
}
