package com.RMS_Backend.Restaurant.Management.System.controller;

import com.RMS_Backend.Restaurant.Management.System.dto.ApiResponse;
import com.RMS_Backend.Restaurant.Management.System.dto.LocationDTO;
import com.RMS_Backend.Restaurant.Management.System.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    // Create a new location
    @PostMapping
    public ResponseEntity<ApiResponse<LocationDTO>> create(@RequestBody LocationDTO dto) {
        log.debug("Entering create location: {}", dto);
        LocationDTO created = locationService.create(dto);
        log.info("Location created with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Location created successfully", created));
    }

    // Update a location
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationDTO>> update(@PathVariable Long id, @RequestBody LocationDTO dto) {
        log.debug("Entering update location id: {}, dto: {}", id, dto);
        dto.setId(id);
        LocationDTO updated = locationService.update(id, dto);
        if (updated == null) {
            log.warn("Location not found for update id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Location not found"));
        }
        log.info("Location updated id: {}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updated));
    }

    // Delete a location
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        log.debug("Entering delete location id: {}", id);
        locationService.delete(id);
        log.info("Location deleted id: {}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("Location deleted successfully"));
    }

    // Get location by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationDTO>> getById(@PathVariable Long id) {
        log.debug("Entering get location by id: {}", id);
        LocationDTO dto = locationService.getById(id);
        if (dto == null) {
            log.warn("Location not found id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Location not found"));
        }
        log.info("Returning location id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Location retrieved successfully", dto));
    }

    // Get all locations
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getAll() {
        log.debug("Entering get all locations");
        List<LocationDTO> list = locationService.getAll();
        log.info("Returning {} locations", list.size());
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", list));
    }
}