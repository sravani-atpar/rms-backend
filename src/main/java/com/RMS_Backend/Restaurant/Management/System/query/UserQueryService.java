package com.RMS_Backend.Restaurant.Management.System.query;


import com.RMS_Backend.Restaurant.Management.System.criteria.UserCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.RMS_Backend.Restaurant.Management.System.model.QAppUser.appUser;
import static com.RMS_Backend.Restaurant.Management.System.model.QUserRoleMapping.userRoleMapping;


/**
 * Service for building QueryDSL Predicates from UserCriteria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    /**
     * Builds a QueryDSL Predicate based on the provided UserCriteria.
     * Each non-null/non-empty field in the criteria is added as an 'AND' condition to the predicate.
     *
     * @param criteria The UserCriteria DTO containing filter parameters.
     * @return A QueryDSL Predicate object.
     */
    public Predicate buildPredicateFromCriteria(UserCriteria criteria) {
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria != null) {
            // User-specific filters
            if (criteria.getId() != null) {
                builder.and(appUser.id.eq(criteria.getId()));
            }
            if (criteria.getKeycloakSubjectId() != null) {
                builder.and(appUser.keycloakSubjectId.containsIgnoreCase(criteria.getKeycloakSubjectId()));
            }
            if (criteria.getFirstName() != null) {
                builder.and(appUser.firstName.containsIgnoreCase(criteria.getFirstName()));
            }
            if (criteria.getLastName() != null) {
                builder.and(appUser.lastName.containsIgnoreCase(criteria.getLastName()));
            }
            if (criteria.getUsername() != null) {
                builder.and(appUser.username.containsIgnoreCase(criteria.getUsername()));
            }
            if (criteria.getEmail() != null) {
                builder.and(appUser.email.containsIgnoreCase(criteria.getEmail()));
            }
            if (criteria.getMobileNumber() != null) {
                builder.and(appUser.mobileNumber.containsIgnoreCase(criteria.getMobileNumber()));
            }
            if (criteria.getIsActive() != null) {
                builder.and(appUser.isActive.eq(criteria.getIsActive()));
            }
            if (criteria.getIsDeleted() != null) {
                builder.and(appUser.isDeleted.eq(criteria.getIsDeleted()));
            }
            
            // Filter by role ID using a subquery
            if (criteria.getRoleId() != null) {
                builder.and(appUser.id.in(
                    JPAExpressions.select(userRoleMapping.appUser.id)
                        .from(userRoleMapping)
                        .where(userRoleMapping.role.id.eq(criteria.getRoleId()))
                ));
            }
        }
        
        log.debug("Built QueryDSL Predicate from criteria: {}", builder.getValue());
        return builder.getValue();
    }
}