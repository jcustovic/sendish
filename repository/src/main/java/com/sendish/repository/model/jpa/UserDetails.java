package com.sendish.repository.model.jpa;

import com.sendish.repository.model.jpa.listener.LocationAware;
import com.sendish.repository.model.jpa.listener.LocationListener;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "auth_user_details")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(LocationListener.class)
public class UserDetails implements Serializable, LocationAware {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "aud_user_id")
    private Long userId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "aud_latitude", nullable = false))
            , @AttributeOverride(name = "longitude", column = @Column(name = "aud_longitude", nullable = false))
            , @AttributeOverride(name = "location", column = @Column(name = "aud_location", nullable = false))
    })
    private Location location;

    @Column(name = "aud_last_location_time", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastLocationTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aud_current_city_id")
    private City currentCity;

    @Column(name = "aud_receive_limit_day", nullable = false)
    private Integer receiveLimitPerDay = 0;

    @Column(name = "aud_today_limit_count", nullable = false)
    private Integer todayReceivedCount = 0;

    @Column(name = "aud_limit_day", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate limitDate;

    @Column(name = "aud_last_interaction_time", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastInteractionTime;

    @Override
    public void buildLocation() {
        if (location != null) {
            location.buildLocation();
        }
    }

    // Getters & setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public DateTime getLastLocationTime() {
        return lastLocationTime;
    }

    public void setLastLocationTime(DateTime lastLocationTime) {
        this.lastLocationTime = lastLocationTime;
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(City currentCity) {
        this.currentCity = currentCity;
    }

    public Integer getReceiveLimitPerDay() {
        return receiveLimitPerDay;
    }

    public void setReceiveLimitPerDay(Integer receiveLimitPerDay) {
        this.receiveLimitPerDay = receiveLimitPerDay;
    }

    public Integer getTodayReceivedCount() {
        return todayReceivedCount;
    }

    public void setTodayReceivedCount(Integer todayReceivedCount) {
        this.todayReceivedCount = todayReceivedCount;
    }

    public LocalDate getLimitDate() {
        return limitDate;
    }

    public void setLimitDate(LocalDate limitDate) {
        this.limitDate = limitDate;
    }

    public DateTime getLastInteractionTime() {
        return lastInteractionTime;
    }

    public void setLastInteractionTime(DateTime lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
    }

}
