package com.RMS_Backend.Restaurant.Management.System.model;


import com.RMS_Backend.Restaurant.Management.System.config.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_log")
@Getter
@Setter
@ToString
public class UserActivityLog extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser user;


    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo; // Optional: Device details

    @Column(name = "session_id")
    private String sessionId; // Optional: Session identifier

    public enum UserActivityType {
        LOGIN,
        LOGOUT,
        // Add other types like PASSWORD_CHANGE, PROFILE_UPDATE, etc. if needed later
    }
}