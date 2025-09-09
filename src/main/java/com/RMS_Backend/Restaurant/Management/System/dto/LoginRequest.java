package com.RMS_Backend.Restaurant.Management.System.dto;


import com.RMS_Backend.Restaurant.Management.System.dto.enums.IdentityType;
import lombok.Data;

@Data
public class LoginRequest {

    private String identity;
    private IdentityType identityType;
    private String password;
    private String otp;
}
