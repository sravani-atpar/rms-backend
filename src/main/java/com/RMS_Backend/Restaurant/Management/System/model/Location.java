package com.RMS_Backend.Restaurant.Management.System.model;

import com.RMS_Backend.Restaurant.Management.System.config.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "locations")
@Audited
public class Location extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locations_seq")
    @SequenceGenerator(name = "locations_seq", sequenceName = "locations_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)


    private String address;

    private String city;

    private String state;

    private String country;

    private String postalCode;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return
//
               Objects.equals(id, location.id) &&
                       Objects.equals(address, location.address) &&
               Objects.equals(city, location.city) ;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, city);
    }
}