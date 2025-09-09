// src/main/java/com/example/awd/farmers/dto/out/UserActivityLogOutDTO.java
package com.RMS_Backend.Restaurant.Management.System.dto.out;


import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserActivityLogOutDTO {
    private Long id;
    private AppUserDTO user; // Details of the user who performed the activity
    private UserActivityLog.UserActivityType activityType;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String deviceInfo;
    private String sessionId;
}