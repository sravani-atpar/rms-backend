package com.RMS_Backend.Restaurant.Management.System.model;


import com.RMS_Backend.Restaurant.Management.System.config.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@Table(name = "user_role_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}))
@Audited
public class UserRoleMapping extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_role_mapping_seq")
    @SequenceGenerator(name = "user_role_mapping_seq", sequenceName = "user_role_mapping_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "is_active")
    private boolean isActive=false;

    @Column(name = "is_deactivated", nullable = false)
    private boolean isDeactivated=false;

    public UserRoleMapping() {}

    public UserRoleMapping(AppUser appUser, Role role) {
        this.appUser = appUser;
        this.role = role;
    }
}
