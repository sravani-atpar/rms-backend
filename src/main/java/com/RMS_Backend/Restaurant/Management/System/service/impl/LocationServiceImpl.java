package com.RMS_Backend.Restaurant.Management.System.service.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.LocationDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.LocationMapping;
import com.RMS_Backend.Restaurant.Management.System.model.Location;
import com.RMS_Backend.Restaurant.Management.System.repository.LocationRepository;
import com.RMS_Backend.Restaurant.Management.System.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapping locationMapping;

    public LocationServiceImpl(LocationRepository locationRepository, LocationMapping locationMapping) {
        this.locationRepository = locationRepository;
        this.locationMapping = locationMapping;
    }

    @Override
    public LocationDTO create(LocationDTO dto) {
        Location location = locationMapping.toDomain(dto);
        Location saved = locationRepository.save(location);
        return locationMapping.toDTO(saved);
    }

    @Override
    public LocationDTO update(Long id, LocationDTO dto) {
        return locationRepository.findById(id)
            .map(existing -> {
                locationMapping.updateDomain(dto, existing);
                return locationMapping.toDTO(locationRepository.save(existing));
            })
            .orElse(null);
    }

    @Override
    public void delete(Long id) {
        locationRepository.deleteById(id);
    }

    @Override
    public LocationDTO getById(Long id) {
        return locationRepository.findById(id).map(locationMapping::toDTO).orElse(null);
    }

    @Override
    public List<LocationDTO> getAll() {
        return locationRepository.findAll().stream().map(locationMapping::toDTO).collect(Collectors.toList());
    }
}