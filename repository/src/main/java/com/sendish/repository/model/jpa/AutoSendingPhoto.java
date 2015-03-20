package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "auto_sending_photo")
@SequenceGenerator(name = "idSequence", sequenceName = "auto_sending_photo_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "asp_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AutoSendingPhoto extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asp_photo_id")
    private Photo photo;

    @Column(name = "asp_active", nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asp_city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asp_country_id")
    private Country country;

    // Getters & setters

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

}
