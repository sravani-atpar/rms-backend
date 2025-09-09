package com.RMS_Backend.Restaurant.Management.System.service.impl;

import com.RMS_Backend.Restaurant.Management.System.config.AbstractAuditingEntity;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.security.SecurityUtils;
import com.RMS_Backend.Restaurant.Management.System.service.AuditingService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static com.RMS_Backend.Restaurant.Management.System.security.Constants.ANONYMOUS;


@Service
public class AuditingServiceImpl implements AuditingService {
    private final AppUserRepository appUserRepository;

    public AuditingServiceImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }


    /**
     * Sets auditing fields for a new entity.
     * Call this before saving a new entity.
     *
     * @param entity The entity to set auditing fields for.
     * @param <T> The type of the entity's ID.
     * @param <E> The type of the entity, must extend AbstractAuditingEntity.
     */
    @Override
    public <T, E extends AbstractAuditingEntity<T>> void setCreationAuditingFields(E entity) {
        String loginKeycloakId = SecurityUtils.getCurrentUserLogin();


        String auditorIdentifier = ANONYMOUS;
        if (loginKeycloakId != null) {
            Optional<AppUser> currentUser = appUserRepository.findByKeycloakSubjectId(loginKeycloakId);
            if (currentUser.isPresent()) {
                auditorIdentifier = currentUser.get().getUsername();
            }
        }


        Timestamp now = Timestamp.from(Instant.now());

        entity.setCreatedBy(auditorIdentifier);
        entity.setCreatedDate(now);
        // On creation, last modified fields might also be set to creation values initially
        entity.setLastModifiedBy(auditorIdentifier);
        entity.setLastModifiedDate(now);
    }

    /**
     * Sets auditing fields for an existing entity during an update.
     * Call this before saving an updated entity.
     *
     * @param entity The entity to set auditing fields for.
     * @param <T> The type of the entity's ID.
     * @param <E> The type of the entity, must extend AbstractAuditingEntity.
     */
    @Override
    public <T, E extends AbstractAuditingEntity<T>> void setUpdateAuditingFields(E entity) {
        String loginKeycloakId = SecurityUtils.getCurrentUserLogin();
        String auditorIdentifier = ANONYMOUS;
        if (loginKeycloakId != null) {
            Optional<AppUser> currentUser = appUserRepository.findByKeycloakSubjectId(loginKeycloakId);
            if (currentUser.isPresent()) {
                auditorIdentifier = currentUser.get().getUsername();
            }
        }

        Timestamp now = Timestamp.from(Instant.now());

        entity.setLastModifiedBy(auditorIdentifier);
        entity.setLastModifiedDate(now);

    }

}
