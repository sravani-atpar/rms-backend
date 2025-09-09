package com.RMS_Backend.Restaurant.Management.System.mapping.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.out.UserActivityLogOutDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserActivityLogMapping;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserMapping;
import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityLogMappinImpl implements UserActivityLogMapping {

    @Autowired
    private UserMapping userMapping;

    @Override
    public UserActivityLogOutDTO toDto(UserActivityLog entity) {
        if (entity == null) {
            return null;
        }

        UserActivityLogOutDTO dto = new UserActivityLogOutDTO();
        dto.setId(entity.getId());

        // Assuming a mapper or conversion method exists for AppUser -> AppUserDTO
        AppUserDTO userDto = userMapping.domainToUserRolesDTO(entity.getUser());
        dto.setUser(userDto);

        if(entity.getActivityType()!=null && !entity.getActivityType().trim().isEmpty()){
            dto.setActivityType(UserActivityLog.UserActivityType.valueOf(entity.getActivityType()));
        }
        dto.setTimestamp(entity.getTimestamp());
        dto.setIpAddress(entity.getIpAddress());
        dto.setDeviceInfo(entity.getDeviceInfo());
        dto.setSessionId(entity.getSessionId());

        return dto;
    }

}
