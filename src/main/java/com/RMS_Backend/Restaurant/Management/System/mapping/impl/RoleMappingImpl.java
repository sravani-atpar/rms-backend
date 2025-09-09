package com.RMS_Backend.Restaurant.Management.System.mapping.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.RoleMapping;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMappingImpl implements RoleMapping {

    @Override
    public RoleDTO toDTO(Role role, boolean active) {
        if (role == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setActive(active);
        return dto;
    }

    @Override
    public Role toDomain(RoleDTO dto) {
        if (dto == null) return null;
        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        return role;
    }

    @Override
    public void updateDomain(RoleDTO dto, Role target) {
        if (dto == null || target == null) return;
        if (dto.getName() != null) target.setName(dto.getName());
        // Note: active flag not applied here; it's part of UserRoleMapping not Role entity
    }
}