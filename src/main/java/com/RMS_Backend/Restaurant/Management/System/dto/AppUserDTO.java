package com.RMS_Backend.Restaurant.Management.System.dto;


import com.RMS_Backend.Restaurant.Management.System.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class AppUserDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String mobileNumber;

    private boolean isActive;
    private boolean isDeleted;
    private List<Role> assignedRoles;

    private List<RoleDTO> rolesWithActiveFlag;

    private String preferredLanguage;

    private Long locationId;

    private LocationDTO location;

}
