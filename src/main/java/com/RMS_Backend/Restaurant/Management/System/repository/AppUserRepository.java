package com.RMS_Backend.Restaurant.Management.System.repository;




import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findById(Long id);

    Optional<AppUser> findByKeycloakSubjectId(String keycloakSubjectId);

    Optional<AppUser> findByUsernameOrEmailOrMobileNumber(String username, String email, String mobileNumber);

    Optional<AppUser> findByUsernameOrMobileNumberAndIsActiveTrue(String username, String mobileNumber);

    Optional<AppUser> findByUsernameOrMobileNumber(String username, String mobileNumber);

    Optional<AppUser> findByMobileNumberAndIsActiveTrue(String mobileNumber);

    Optional<AppUser> findByUsernameAndIsActiveTrue(String username);

    List<AppUser> findByUsernameIsNotNull();

    List<AppUser> findByUsernameIsNotNullOrderById();




}