package com.sendish.repository.model.jpa;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "auth_user_statistics")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "aus_user_id")
    private Long userId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Column(name = "aus_likes_count", nullable = false)
    private Integer likes = 0;

    @Column(name = "aus_dislikes_count", nullable = false)
    private Integer dislikes = 0;

    @Column(name = "aus_cities_count", nullable = false)
    private Integer cities = 0;

    @Column(name = "aus_reports_count", nullable = false)
    private Integer reports = 0;

    @Column(name = "aus_rank")
    private Integer rank;

    @Column(name = "aus_modified_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime modifiedDate;

    @PreUpdate
    public final void updateModifyDate() {
        modifiedDate = DateTime.now();
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

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }

    public Integer getCities() {
        return cities;
    }

    public void setCities(Integer cities) {
        this.cities = cities;
    }

    public Integer getReports() {
        return reports;
    }

    public void setReports(Integer reports) {
        this.reports = reports;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
