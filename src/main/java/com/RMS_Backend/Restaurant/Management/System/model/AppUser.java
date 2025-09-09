package com.RMS_Backend.Restaurant.Management.System.model;



import com.RMS_Backend.Restaurant.Management.System.config.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "app_users")
@Audited
public class AppUser extends AbstractAuditingEntity<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_users_seq")
    @SequenceGenerator(name = "app_users_seq", sequenceName = "app_users_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true)
    private String keycloakSubjectId;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String username;

    private String email;

    private String mobileNumber;



    private boolean isActive;

    private boolean isDeleted;

    @Column(columnDefinition = "TEXT")
    private String deviceMetaData;

    private String preferredLanguage;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUser appUser = (AppUser) o;
        return isActive() == appUser.isActive() && Objects.equals(id, appUser.id) && Objects.equals(keycloakSubjectId, appUser.keycloakSubjectId) && Objects.equals(username, appUser.username) && Objects.equals(email, appUser.email) && Objects.equals(mobileNumber, appUser.mobileNumber) && Objects.equals(deviceMetaData, appUser.deviceMetaData) && Objects.equals(preferredLanguage, appUser.preferredLanguage) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keycloakSubjectId, username, email, mobileNumber, isActive);
    }
}
