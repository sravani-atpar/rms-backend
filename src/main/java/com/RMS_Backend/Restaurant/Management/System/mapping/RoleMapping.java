package com.RMS_Backend.Restaurant.Management.System.mapping;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.model.Role;

public interface RoleMapping {

    // Convert entity to DTO
    RoleDTO toDTO(Role role, boolean active);

    // Convert DTO to entity (active flag lives in mapping table, so ignore here)
    Role toDomain(RoleDTO dto);

    // Update existing entity with non-null fields from DTO
    void updateDomain(RoleDTO dto, Role target);
}
