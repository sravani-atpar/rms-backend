// src/main/java/com/example/awd/farmers/service/UserActivityLogService.java
package com.RMS_Backend.Restaurant.Management.System.service;


import com.RMS_Backend.Restaurant.Management.System.dto.out.UserActivityLogOutDTO;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserActivityLogService {

    /**
     * Records a user activity log entry.
     *
     * @param user The AppUser performing the activity.
     * @param activityType The type of activity (e.g., LOGIN, LOGOUT).
     * @param sessionId The session identifier (can be null).
     * @param ipAddress The IP address from which the activity originated (can be null).
     * @param deviceInfo Information about the user's device/client (can be null).
     */
    void recordActivity(AppUser user, UserActivityLog.UserActivityType activityType, String sessionId, String ipAddress, String deviceInfo);

    /**
     * Retrieves user activity logs with pagination.
     * Access control for viewing logs should be handled by the caller (e.g., Controller).
     *
     * @param pageable Pagination information.
     * @return A page of UserActivityLogOutDTOs.
     */
    Page<UserActivityLogOutDTO> getAllActivityLogs(Pageable pageable);

    /**
     * Retrieves user activity logs for a specific user with pagination.
     * Access control for viewing logs should be handled by the caller (e.g., Controller).
     *
     * @param userId The ID of the user.
     * @param pageable Pagination information.
     * @return A page of UserActivityLogOutDTOs.
     */
    Page<UserActivityLogOutDTO> getActivityLogsByUser(Long userId, Pageable pageable);

    /**
     * Retrieves user activity logs by type with pagination.
     * Access control for viewing logs should be handled by the caller (e.g., Controller).
     *
     * @param activityType The type of activity.
     * @param pageable Pagination information.
     * @return A page of UserActivityLogOutDTOs.
     */
    Page<UserActivityLogOutDTO> getActivityLogsByType(UserActivityLog.UserActivityType activityType, Pageable pageable);

    // Add other retrieval methods as needed (e.g., by date range, by user and type)
}