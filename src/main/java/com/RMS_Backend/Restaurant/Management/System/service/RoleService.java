package com.RMS_Backend.Restaurant.Management.System.service;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.SyncUserRolesDTO;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;

import java.util.List;

public interface RoleService {

    List<UserRoleMapping> getUserRoleMappingsByUser(Long appUserId);

    RoleDTO create(RoleDTO dto);

    RoleDTO update(Long id, RoleDTO dto);

    void delete(Long id);

    RoleDTO getById(Long id);

    List<RoleDTO> getAll();
    void syncRolesWithUser(SyncUserRolesDTO syncUserRolesDTO );

    boolean assignRolesToAppUser(Long appUserId, Role role);

    UserRoleMapping assignRoleToUser(Long appUserId, Long roleId);
    void saveUserRoleMapping(UserRoleMapping userRoleMapping);
}
