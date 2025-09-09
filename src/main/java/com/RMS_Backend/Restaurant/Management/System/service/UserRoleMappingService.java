package com.RMS_Backend.Restaurant.Management.System.service;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;

import java.util.List;

public interface UserRoleMappingService {

    // Assign a role to user
    void assignRole(Long userId, Long roleId, boolean active);

    // Remove role from user (soft-deactivate or hard delete)
    void removeRole(Long userId, Long roleId);

    // Activate/deactivate a user-role mapping
    void updateActive(Long userId, Long roleId, boolean active);

    // Get roles for a user as RoleDTOs (active flag included)
    List<RoleDTO> getRolesForUser(Long userId, boolean onlyActive);
}
