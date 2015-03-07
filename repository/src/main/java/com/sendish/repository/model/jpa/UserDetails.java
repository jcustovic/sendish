package com.sendish.repository.model.jpa;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.sendish.repository.model.jpa.listener.LocationAware;
import com.sendish.repository.model.jpa.listener.LocationListener;

@Entity
@Table(name = "auth_user_details")
@EntityListeners(LocationListener.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
            @AttributeOverride(name = "latitude", column = @Column(name = "aud_latitude"))
            , @AttributeOverride(name = "longitude", column = @Column(name = "aud_longitude"))
            , @AttributeOverride(name = "location", column = @Column(name = "aud_location"))
    })
    private Location location;

    @Column(name = "aud_last_location_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastLocationTime;

    @ManyToOne
    @JoinColumn(name = "aud_current_city_id")
    private City currentCity;

    @Column(name = "aud_receive_limit_day", nullable = false)
    private Integer receiveLimitPerDay;

    @Column(name = "aud_send_limit_day", nullable = false)
    private Integer sendLimitPerDay;

    @Column(name = "aud_receive_notifications", nullable = false)
    private Boolean receiveNotifications = true;

    @Column(name = "aud_last_received_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastReceivedTime;

    @Column(name = "aud_last_sent_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastSentTime;

    @Column(name = "aud_receive_allowed_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime receiveAllowedTime;

    @Column(name = "aud_send_allowed_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime sendAllowedTime;

    @Column(name = "aud_last_interaction_time")
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

    public Integer getSendLimitPerDay() {
        return sendLimitPerDay;
    }

    public void setSendLimitPerDay(Integer sendLimitPerDay) {
        this.sendLimitPerDay = sendLimitPerDay;
    }

    public Boolean getReceiveNotifications() {
        return receiveNotifications;
    }

    public void setReceiveNotifications(Boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }

    public DateTime getLastReceivedTime() {
        return lastReceivedTime;
    }

    public void setLastReceivedTime(DateTime lastReceivedTime) {
        this.lastReceivedTime = lastReceivedTime;
    }

    public DateTime getLastSentTime() {
        return lastSentTime;
    }

    public void setLastSentTime(DateTime lastSentTime) {
        this.lastSentTime = lastSentTime;
    }

    public DateTime getReceiveAllowedTime() {
        return receiveAllowedTime;
    }

    public void setReceiveAllowedTime(DateTime receiveAllowedTime) {
        this.receiveAllowedTime = receiveAllowedTime;
    }

    public DateTime getSendAllowedTime() {
        return sendAllowedTime;
    }

    public void setSendAllowedTime(DateTime sendAllowedTime) {
        this.sendAllowedTime = sendAllowedTime;
    }

    public DateTime getLastInteractionTime() {
        return lastInteractionTime;
    }

    public void setLastInteractionTime(DateTime lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
    }

}
