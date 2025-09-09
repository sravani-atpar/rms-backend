package com.RMS_Backend.Restaurant.Management.System.dto.out;

import com.RMS_Backend.Restaurant.Management.System.dto.LocationDTO;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class KeycloakAppUserOutDTO {

    private String keycloakId;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String mobileNumber;

    private String govtIdType;

    private String govtIdNumber;

    private boolean isActive;
    private List<Role> assignedRoles;

    private String preferredLanguage;

    private Long locationId;

    private LocationDTO location;
}
