package com.RMS_Backend.Restaurant.Management.System.criteria;

import lombok.*;

/**
 * Criteria class for filtering users.
 * This class is used to build QueryDSL predicates for searching users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Useful for comparing criteria objects in tests/logs
@ToString // Useful for logging
public class UserCriteria {

    private Long id;
    private String keycloakSubjectId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String mobileNumber;
    private Boolean isActive;
    private Boolean isDeleted;
    private Long roleId; // For filtering users by role
}