package com.RMS_Backend.Restaurant.Management.System.controller;

import com.RMS_Backend.Restaurant.Management.System.dto.ApiResponse;
import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.LoginRequest;
import com.RMS_Backend.Restaurant.Management.System.dto.RegisterRequest;
import com.RMS_Backend.Restaurant.Management.System.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {



    @Autowired
    private UserService userService;






    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AppUserDTO>> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Entering register method for identity: {} with type {}", registerRequest.getIdentity(), registerRequest.getIdentityType());
//        otpService.verifyOtp(registerRequest.getIdentity(), registerRequest.getIdentityType(), registerRequest.getOtp());

        AppUserDTO appUser =userService.registerUser(registerRequest);

        log.info("User with identity {} and type {} registered successfully.", registerRequest.getIdentity(), registerRequest.getIdentityType());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully.",appUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(@RequestBody LoginRequest loginRequest) {
        log.info("Entering login method for identity: {} with type {}", loginRequest.getIdentity(), loginRequest.getIdentityType());
//        if(loginRequest.getIdentityType().equals(Otp.IdentityType.MOBILE)) {
//            otpService.verifyOtp(loginRequest.getIdentity(), loginRequest.getIdentityType(), loginRequest.getOtp());
//        }



//        if(loginRequest.getIdentityType().equals(Otp.IdentityType.MOBILE)){
//            try {
//                //AccessTokenResponse response = authzClient.obtainAccessToken(loginRequest.getIdentity(),loginRequest.getOtp());
//                AccessTokenResponse response = authzClient.obtainAccessToken(loginRequest.getIdentity(),loginRequest.getPassword());
//                return ResponseEntity.ok(ApiResponse.success(response));
//            } catch (Exception e) {
//                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Authentication failed: " + e.getMessage()));
//            }
//        }else if(loginRequest.getIdentityType().equals(Otp.IdentityType.EMAIL)){
//            try {
//                AccessTokenResponse response = authzClient.obtainAccessToken(loginRequest.getIdentity(),loginRequest.getPassword());
//                return ResponseEntity.ok(ApiResponse.success(response));
//            } catch (Exception e) {
//                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Authentication failed: " + e.getMessage()));
//            }
//
//        } else if (loginRequest.getIdentityType().equals(Otp.IdentityType.USERNAME)) {
//            try {
//                AccessTokenResponse response = authzClient.obtainAccessToken(loginRequest.getIdentity(),loginRequest.getPassword());
//                return ResponseEntity.ok(ApiResponse.success(response));
//            } catch (Exception e) {
//                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Authentication failed: " + e.getMessage()));
//            }
//        }
//        else{
//            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("IdentityType is not matching: " + loginRequest.getIdentityType().name()));
//        }

        AccessTokenResponse token = userService.login(loginRequest.getIdentity(),loginRequest.getIdentityType().name(),loginRequest.getPassword());
        log.info("User with identity {} and type {} logged in successfully.", loginRequest.getIdentity(), loginRequest.getIdentityType());



//        webSocketPushService.sendGreetingToAll("Welcome " + loginRequest.getIdentity());
        return ResponseEntity.ok(ApiResponse.success("User logged in successfully.", token));
    }

    // Endpoint that clients will call to logout.
    // Spring Security's filter chain intercepts this URL BEFORE this method body executes.
    // The actual logout logic is handled by Spring Security and the LogoutSuccessHandler.
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.debug("Logout endpoint hit. Spring Security will handle the actual logout and delegate to LogoutSuccessHandler.");
        return ResponseEntity.ok(ApiResponse.success("Logout process initiated.", null));
    }


    @PostMapping("/get-token/by-refreshToken")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> getAccessToken(@RequestParam String refreshToken) {

        AccessTokenResponse token = userService.getAccessToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token retrieved successfully.", token));
    }
}