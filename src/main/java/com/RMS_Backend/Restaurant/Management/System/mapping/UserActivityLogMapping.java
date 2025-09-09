package com.RMS_Backend.Restaurant.Management.System.mapping;


import com.RMS_Backend.Restaurant.Management.System.dto.out.UserActivityLogOutDTO;
import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;

public interface UserActivityLogMapping {

    UserActivityLogOutDTO toDto(UserActivityLog entity);
}
