package com.RMS_Backend.Restaurant.Management.System.mapping.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.*;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserMapping;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Location;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import com.RMS_Backend.Restaurant.Management.System.repository.RoleRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.UserRoleMappingRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMappingImpl implements UserMapping {


    private final UserRoleMappingRepository userRoleMappingRepository;

    public UserMappingImpl(RoleRepository roleRepository, UserRoleMappingRepository userRoleMappingRepository) {
        this.userRoleMappingRepository = userRoleMappingRepository;
    }

    @Override
    public AppUser RequestToDomain(RegisterRequest registerRequest) {
        AppUser appUser = new AppUser();
        appUser.setFirstName(registerRequest.getFirstName());
        appUser.setLastName(registerRequest.getLastName());
        appUser.setUsername(registerRequest.getUsername());
        appUser.setEmail(registerRequest.getEmail());
        appUser.setMobileNumber(registerRequest.getMobileNumber());

        appUser.setPreferredLanguage(registerRequest.getPreferredLanguage());

        // Set location if locationId is provided
        if (registerRequest.getLocationId() != null) {
            Location location = new Location();
            location.setId(registerRequest.getLocationId());
            appUser.setLocation(location);
        }

        return appUser;
    }

    @Override
    public AppUserDTO domainToUserRolesDTO(AppUser appUser) {
        if (appUser == null) {
            return null;
        }

        List<Role> userRoles = new ArrayList<>();

        AppUserDTO dto = new AppUserDTO();
        dto.setId(appUser.getId());
        dto.setFirstName(appUser.getFirstName());
        dto.setLastName(appUser.getLastName());
        dto.setUsername(appUser.getUsername());
        dto.setEmail(appUser.getEmail());
        dto.setActive(appUser.isActive());
        dto.setDeleted(appUser.isDeleted());
        dto.setMobileNumber(appUser.getMobileNumber());

        dto.setPreferredLanguage(appUser.getPreferredLanguage());

        // Set location information
        if (appUser.getLocation() != null) {
            dto.setLocationId(appUser.getLocation().getId());

            // Create LocationDTO with minimal information
            LocationDTO locationDTO = new LocationDTO(
                appUser.getLocation().getId(),

                    appUser.getLocation().getAddress(),
                    appUser.getLocation().getCity(),
                    appUser.getLocation().getState(),
                    appUser.getLocation().getCountry(),
                    appUser.getLocation().getPostalCode()

            );
            dto.setLocation(locationDTO);
        }
        List<UserRoleMapping> userRoleMappingList =userRoleMappingRepository.findByAppUserIdAndIsDeactivatedFalse(appUser.getId());
        List<RoleDTO> userRolesDTO = new ArrayList<>();
        for (UserRoleMapping userRoleMapping : userRoleMappingList) {
            userRoles.add(userRoleMapping.getRole());
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setId(userRoleMapping.getRole().getId());
            roleDTO.setName(userRoleMapping.getRole().getName());
            roleDTO.setActive(userRoleMapping.isActive());
            userRolesDTO.add(roleDTO);
        }
        dto.setRolesWithActiveFlag(userRolesDTO);
        dto.setAssignedRoles(userRoles);
        return dto;
    }


}
