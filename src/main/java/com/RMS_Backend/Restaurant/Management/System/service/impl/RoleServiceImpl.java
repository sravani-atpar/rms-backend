package com.RMS_Backend.Restaurant.Management.System.service.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.SyncUserRolesDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.RoleMapping;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.RoleRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.UserRoleMappingRepository;
import com.RMS_Backend.Restaurant.Management.System.security.Constants;
import com.RMS_Backend.Restaurant.Management.System.service.AuditingService;
import com.RMS_Backend.Restaurant.Management.System.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;



@Service

@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapping roleMapping;
    private final UserRoleMappingRepository roleMappingRepository;
    private final AuditingService auditingService;
    private final AppUserRepository appUserRepository;


    public RoleServiceImpl(RoleRepository roleRepository, RoleMapping roleMapping, UserRoleMappingRepository roleMappingRepository, AuditingService auditingService, AppUserRepository appUserRepository) {
        this.roleRepository = roleRepository;
        this.roleMapping = roleMapping;
        this.roleMappingRepository = roleMappingRepository;
        this.auditingService = auditingService;

        this.appUserRepository = appUserRepository;
    }

    @Override
    public List<UserRoleMapping> getUserRoleMappingsByUser(Long appUserId) {
        return roleMappingRepository.findByAppUserId(appUserId);
    }
    @Override
    public void saveUserRoleMapping(UserRoleMapping userRoleMapping) {
        auditingService.setUpdateAuditingFields(userRoleMapping);
        roleMappingRepository.save(userRoleMapping);
    }

    @Override
    public RoleDTO create(RoleDTO dto) {
        Role role = roleMapping.toDomain(dto);
        Role saved = roleRepository.save(role);
        // Assuming newly created roles are active by default when presented in DTO layer
        return roleMapping.toDTO(saved, true);
    }

    @Override
    public void syncRolesWithUser(SyncUserRolesDTO syncUserRolesDTO) {
        // Fetch existing role mappings for the user
        List<UserRoleMapping> existingRoleMappings = getUserRoleMappingsByUser(syncUserRolesDTO.getAppUserId());

        // Get the existing active role IDs from the existing role mappings
        Set<Long> existingActiveRoleIds = existingRoleMappings.stream()
                .filter(mapping -> !mapping.isDeactivated()) // Only consider active mappings for 'existing'
                .map(mapping -> mapping.getRole().getId())
                .collect(Collectors.toSet());

        // Get all role IDs from the newRoles set (desired state)
        Set<Long> newRoleIds = syncUserRolesDTO.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        // Determine roles to deactivate/remove (existing active roles not in the new set)
        Set<Long> rolesToDeactivateOrRemove = new HashSet<>(existingActiveRoleIds);
        rolesToDeactivateOrRemove.removeAll(newRoleIds);

        // Determine roles to activate/add (new roles not currently active)
        Set<Long> rolesToActivateOrAdd = new HashSet<>(newRoleIds);
        rolesToActivateOrAdd.removeAll(existingActiveRoleIds);

        // Process roles to deactivate/remove
        for (Long roleId : rolesToDeactivateOrRemove) {
            Optional<UserRoleMapping> mappingOptional = roleMappingRepository.findByAppUserIdAndRoleId(syncUserRolesDTO.getAppUserId(), roleId);
            if (mappingOptional.isPresent()) {
                UserRoleMapping mapping = mappingOptional.get();
                if (!mapping.isDeactivated()) { // Double-check if it's active before deactivating
                    mapping.setActive(false);
                    mapping.setDeactivated(true);
                    auditingService.setUpdateAuditingFields(mapping); // Set update auditing fields for deactivation
                    roleMappingRepository.save(mapping);
                }
            }
        }

        // Process roles to activate/add
        for (Long roleId : rolesToActivateOrAdd) {
            // Re-use assignRoleToUser, which handles both new creation and reactivation,
            // and applies auditing appropriately.
            assignRoleToUser(syncUserRolesDTO.getAppUserId(), roleId);
        }
        log.info("Roles synced for AppUser ID: {}", syncUserRolesDTO.getAppUserId());
    }

    @Override
    public RoleDTO update(Long id, RoleDTO dto) {
        return roleRepository.findById(id)
                .map(existing -> {
                    roleMapping.updateDomain(dto, existing);
                    return roleMapping.toDTO(roleRepository.save(existing), dto.isActive());
                })
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public RoleDTO getById(Long id) {
        return roleRepository.findById(id).map(r -> roleMapping.toDTO(r, true)).orElse(null);
    }

    @Override
    public List<RoleDTO> getAll() {
        return roleRepository.findAll().stream().map(r -> roleMapping.toDTO(r, true)).collect(Collectors.toList());
    }

    @Override
    public boolean assignRolesToAppUser(Long appUserId, Role role) {
        try {
            assignRoleToUser(appUserId, role.getId());
        } catch (Exception e) {
            log.error("Failed to assign role: " + e.getMessage());
            return false;
        }
        return true; // All roles assigned successfully
    }


    @Override
    public UserRoleMapping assignRoleToUser(Long appUserId, Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + roleId));
        AppUser appUser = appUserRepository.findById(appUserId).orElseThrow(() -> new EntityNotFoundException("AppUser not found with ID: " + appUserId));

        Role anonymousRole = roleRepository.findByName(Constants.ANONYMOUS).orElseThrow(() -> new EntityNotFoundException("Anonymous role not found in the roles."));

        UserRoleMapping roleMapping = new UserRoleMapping();
        Optional<UserRoleMapping> userRoleMappingOptional = roleMappingRepository.findByAppUserIdAndRoleId(appUserId, roleId);

        if (userRoleMappingOptional.isPresent()) {
            roleMapping = userRoleMappingOptional.get();

            if (!roleMapping.isDeactivated()) {
                log.warn("User Role :" + roleMapping.getRole().getName() + " already active for the user id: " + appUserId);
                return roleMapping; // Return existing active mapping without changes
            } else {
                roleMapping.setActive(true);
                roleMapping.setDeactivated(false);
                auditingService.setUpdateAuditingFields(roleMapping); // Set update auditing fields for reactivation
            }
        } else {

            roleMapping.setRole(role);
            roleMapping.setAppUser(appUser);
            roleMapping.setActive(true);
            roleMapping.setDeactivated(false);
            auditingService.setCreationAuditingFields(roleMapping); // Set creation auditing fields for new mapping
        }
        Optional<UserRoleMapping> anonymousRoleMappingOptional = roleMappingRepository.findByAppUserIdAndRoleId(appUserId, anonymousRole.getId());
        if (anonymousRoleMappingOptional.isPresent()) {
            UserRoleMapping userAnonymousRoleMapping = anonymousRoleMappingOptional.get();
            roleMapping.setActive(false);
            userAnonymousRoleMapping.setDeactivated(true);
            auditingService.setUpdateAuditingFields(userAnonymousRoleMapping);
            roleMappingRepository.save(userAnonymousRoleMapping);
        }
        return roleMappingRepository.save(roleMapping);
    }
}