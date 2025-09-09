package com.RMS_Backend.Restaurant.Management.System.repository;


import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {

    
    void deleteByAppUserId(Long appUserId);

    Optional<UserRoleMapping> findByAppUserIdAndRoleId(Long appUserId, Long roleId);

    /**
     * Find a user role mapping by app user ID and role name
     * @param appUserId the app user ID
     * @param roleName the role name
     * @return optional of user role mapping
     */
    @Query("SELECT urm FROM UserRoleMapping urm WHERE urm.appUser.id = :appUserId AND urm.role.name = :roleName")
    Optional<UserRoleMapping> findByAppUserIdAndRoleName(@Param("appUserId") Long appUserId, @Param("roleName") String roleName);

    List<UserRoleMapping> findByAppUserId(Long appUserId);

    List<UserRoleMapping> findByAppUserIdAndIsDeactivatedFalse(Long appUserId);

    List<UserRoleMapping> findByAppUserIdAndIsActiveTrue(Long appUserId);

    List<UserRoleMapping> findByRoleId(Long roleId);

    Optional<UserRoleMapping> findByAppUserIdAndRoleIdAndIsDeactivatedFalse(Long id, Long id1);
}
