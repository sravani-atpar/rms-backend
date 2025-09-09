package com.RMS_Backend.Restaurant.Management.System.mapping;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;

public interface UserRoleMappingMapping {

    // Create a new UserRoleMapping from parts
    UserRoleMapping create(AppUser user, Role role, boolean active);

    // Convert mapping to RoleDTO (active flag comes from mapping)
    RoleDTO toRoleDTO(UserRoleMapping mapping);

    // Update mapping flags
    void updateFlags(UserRoleMapping mapping, boolean active, boolean deactivated);
}
