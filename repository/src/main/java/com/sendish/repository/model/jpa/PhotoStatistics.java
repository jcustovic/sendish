package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "photo_statistics")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PhotoStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "pst_photo_id")
    private Long photoId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Photo photo;

    @Column(name = "pst_likes_count", nullable = false)
    private Integer likes = 0;

    @Column(name = "pst_dislikes_count", nullable = false)
    private Integer dislikes = 0;

    @Column(name = "pst_reports_count", nullable = false)
    private Integer reports = 0;

    @Column(name = "pst_cities_count", nullable = false)
    private Integer cities = 0;

    @Column(name = "pst_countries_count", nullable = false)
    private Integer countries = 0;

    @Column(name = "pst_comments_count", nullable = false)
    private Integer comments = 0;

    @Column(name = "pst_users_count", nullable = false)
    private Integer users = 0;

    @Column(name = "pst_modified_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime modifiedDate;

    @PreUpdate
    @PrePersist
    public final void updateModifyDate() {
        modifiedDate = DateTime.now();
    }

    // Getters & setters

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
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

    public Integer getReports() {
        return reports;
    }

    public void setReports(Integer reports) {
        this.reports = reports;
    }

    public Integer getCities() {
        return cities;
    }

    public void setCities(Integer cities) {
        this.cities = cities;
    }

    public Integer getCountries() {
        return countries;
    }

    public void setCountries(Integer countries) {
        this.countries = countries;
    }

    public Integer getUsers() {
        return users;
    }

    public void setUsers(Integer users) {
        this.users = users;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
