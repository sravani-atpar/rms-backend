package com.RMS_Backend.Restaurant.Management.System.service.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserRoleMappingMapping;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.RoleRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.UserRoleMappingRepository;
import com.RMS_Backend.Restaurant.Management.System.service.UserRoleMappingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserRoleMappingServiceImpl implements UserRoleMappingService {

    private final UserRoleMappingRepository userRoleMappingRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingMapping mapping;

    public UserRoleMappingServiceImpl(UserRoleMappingRepository userRoleMappingRepository,
                                      AppUserRepository appUserRepository,
                                      RoleRepository roleRepository,
                                      UserRoleMappingMapping mapping) {
        this.userRoleMappingRepository = userRoleMappingRepository;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.mapping = mapping;
    }

    @Override
    public void assignRole(Long userId, Long roleId, boolean active) {
        Optional<UserRoleMapping> existing = userRoleMappingRepository.findByAppUserIdAndRoleId(userId, roleId);
        if (existing.isPresent()) {
            // Update flags if mapping already exists
            UserRoleMapping m = existing.get();
            mapping.updateFlags(m, active, false);
            userRoleMappingRepository.save(m);
            return;
        }
        AppUser user = appUserRepository.findById(userId).orElse(null);
        Role role = roleRepository.findById(roleId).orElse(null);
        if (user == null || role == null) return;
        UserRoleMapping m = mapping.create(user, role, active);
        userRoleMappingRepository.save(m);
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        userRoleMappingRepository.findByAppUserIdAndRoleId(userId, roleId).ifPresent(userRoleMappingRepository::delete);
    }

    @Override
    public void updateActive(Long userId, Long roleId, boolean active) {
        userRoleMappingRepository.findByAppUserIdAndRoleId(userId, roleId)
            .ifPresent(m -> {
                mapping.updateFlags(m, active, !active && m.isDeactivated());
                userRoleMappingRepository.save(m);
            });
    }

    @Override
    public List<RoleDTO> getRolesForUser(Long userId, boolean onlyActive) {
        List<UserRoleMapping> mappings = onlyActive
            ? userRoleMappingRepository.findByAppUserIdAndIsActiveTrue(userId)
            : userRoleMappingRepository.findByAppUserIdAndIsDeactivatedFalse(userId);
        return mappings.stream().map(mapping::toRoleDTO).collect(Collectors.toList());
    }
}