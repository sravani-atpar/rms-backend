// src/main/java/com/example/awd/farmers/repository/UserActivityLogRepository.java
package com.RMS_Backend.Restaurant.Management.System.repository;


import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {


    Page<UserActivityLog> findByUserId(Long userId, Pageable pageable);


    Page<UserActivityLog> findByActivityType(UserActivityLog.UserActivityType activityType, Pageable pageable);


    Page<UserActivityLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);


    Page<UserActivityLog> findByActivityTypeAndTimestampBetween(UserActivityLog.UserActivityType activityType, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<UserActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);


}