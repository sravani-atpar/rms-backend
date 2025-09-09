package com.RMS_Backend.Restaurant.Management.System.dto;


import com.RMS_Backend.Restaurant.Management.System.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class SyncUserRolesDTO {

    private Long appUserId;
    private List<Role> roles;
}
