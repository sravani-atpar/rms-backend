
package com.RMS_Backend.Restaurant.Management.System.service.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.out.UserActivityLogOutDTO;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserActivityLogMapping;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.UserActivityLogRepository;
import com.RMS_Backend.Restaurant.Management.System.service.AuditingService;
import com.RMS_Backend.Restaurant.Management.System.service.UserActivityLogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogRepository userActivityLogRepository;
    private final AppUserRepository appUserRepository; // Need this to potentially fetch AppUser entity
    private final UserActivityLogMapping userActivityLogMapper;
    private final AuditingService auditingService;


    @Override
    @Transactional
    public void recordActivity(AppUser user, UserActivityLog.UserActivityType activityType, String sessionId, String ipAddress, String deviceInfo) {
        // Defensive check: ensure user is not null
        if (user == null) {
            log.error("Cannot record activity for a null user. Activity Type: {}", activityType);
            return; // Or throw an exception, depending on desired behavior
        }

        UserActivityLog logEntry = new UserActivityLog();
        logEntry.setUser(user); // Use the actual AppUser entity
        logEntry.setActivityType(activityType.name());
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setSessionId(sessionId);
        logEntry.setIpAddress(ipAddress);
        logEntry.setDeviceInfo(deviceInfo);
        auditingService.setCreationAuditingFields(logEntry);
        userActivityLogRepository.save(logEntry);
        log.debug("Recorded activity '{}' for user {} (ID: {})", activityType, user.getUsername(), user.getId());
    }

    @Override
    @Transactional
    public Page<UserActivityLogOutDTO> getAllActivityLogs(Pageable pageable) {
        // Note: Implement access control in Controller/Security Layer
        log.debug("Fetching all activity logs pageable: {}", pageable);
        Page<UserActivityLog> logs = userActivityLogRepository.findAll(pageable);
        return logs.map(userActivityLogMapper::toDto);
    }

    @Override
    @Transactional
    public Page<UserActivityLogOutDTO> getActivityLogsByUser(Long userId, Pageable pageable) {

        log.debug("Fetching activity logs for user ID {} pageable: {}", userId, pageable);

        AppUser appUser =appUserRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

        Page<UserActivityLog> logs = userActivityLogRepository.findByUserId(appUser.getId(), pageable);
        return logs.map(userActivityLogMapper::toDto);
    }

    @Override
    @Transactional
    public Page<UserActivityLogOutDTO> getActivityLogsByType(UserActivityLog.UserActivityType activityType, Pageable pageable) {
        // Note: Implement access control in Controller/Security Layer
        log.debug("Fetching activity logs by type {} pageable: {}", activityType, pageable);
        Page<UserActivityLog> logs = userActivityLogRepository.findByActivityType(activityType, pageable);
        return logs.map(userActivityLogMapper::toDto);
    }

    // Implement other retrieval methods if needed
}