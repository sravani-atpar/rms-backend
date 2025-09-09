package com.RMS_Backend.Restaurant.Management.System.mapping;


import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.RegisterRequest;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;

public interface UserMapping {

    AppUser RequestToDomain(RegisterRequest registerRequest);

    AppUserDTO domainToUserRolesDTO(AppUser appUser);


}
