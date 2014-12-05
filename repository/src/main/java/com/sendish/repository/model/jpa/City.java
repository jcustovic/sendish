package com.sendish.repository.model.jpa;

import com.sendish.repository.model.jpa.listener.LocationAware;
import com.sendish.repository.model.jpa.listener.LocationListener;
import org.hibernate.annotations.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "city")
@SequenceGenerator(name = "idSequence", sequenceName = "city_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "ct_id"))
@EntityListeners(LocationListener.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class City extends BaseEntity implements LocationAware {

    private static final long serialVersionUID = 1L;

    @Column(name = "ct_external_id", nullable = false, unique = true)
    private Integer externalId;

    @Column(name = "ct_name", nullable = false, length = 200)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "ct_latitude", nullable = false))
            , @AttributeOverride(name = "longitude", column = @Column(name = "ct_longitude", nullable = false))
            , @AttributeOverride(name = "location", column = @Column(name = "ct_location", nullable = false))
    })
    private Location location;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ct_country_id")
    private Country country;

    @Column(name = "ct_population")
    private Integer population;

    @Column(name = "ct_timezone", nullable = false)
    private String timezone;

    @Column(name = "ct_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = DateTime.now();
    }

    @Override
    public void buildLocation() {
        if (location != null) {
            location.buildLocation();
        }
    }

    // Getters & setters

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

}
