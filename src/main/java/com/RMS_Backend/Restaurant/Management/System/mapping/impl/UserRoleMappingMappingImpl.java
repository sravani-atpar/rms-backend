package com.RMS_Backend.Restaurant.Management.System.mapping.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserRoleMappingMapping;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMappingMappingImpl implements UserRoleMappingMapping {

    @Override
    public UserRoleMapping create(AppUser user, Role role, boolean active) {
        UserRoleMapping mapping = new UserRoleMapping();
        mapping.setAppUser(user);
        mapping.setRole(role);
        mapping.setActive(active);
        mapping.setDeactivated(false);
        return mapping;
    }

    @Override
    public RoleDTO toRoleDTO(UserRoleMapping mapping) {
        if (mapping == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setId(mapping.getRole().getId());
        dto.setName(mapping.getRole().getName());
        dto.setActive(mapping.isActive());
        return dto;
    }

    @Override
    public void updateFlags(UserRoleMapping mapping, boolean active, boolean deactivated) {
        if (mapping == null) return;
        mapping.setActive(active);
        mapping.setDeactivated(deactivated);
    }
}