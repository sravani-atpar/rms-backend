package com.RMS_Backend.Restaurant.Management.System.dto;


import com.RMS_Backend.Restaurant.Management.System.dto.enums.IdentityType;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    private String identity;
    private IdentityType identityType;

    private String otp;
    private String password;
    private String firstName;

    private String lastName;

    private String username;

    private String email;

    @NotNull
    private String mobileNumber;

    private Role appRole;

    private String preferredLanguage;

    private Long locationId;
}
