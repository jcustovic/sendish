package com.sendish.repository.model.jpa;

import com.sendish.repository.model.jpa.listener.LocationListener;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "photo_receiver")
@SequenceGenerator(name = "idSequence", sequenceName = "photo_receiver_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "pr_id"))
@EntityListeners(LocationListener.class)
public class PhotoReceiver extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pr_photo_id")
    @Fetch(FetchMode.JOIN)
    private Photo photo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pr_user_id")
    private User user;

    @Column(name = "pr_like")
    private Boolean like;

    @Column(name = "pr_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "pr_report")
    private Boolean report;

    @Column(name = "pr_report_type", length = 32)
    private String reportType;

    @Column(name = "pr_report_text", length = 128)
    private String reportText;

    @Column(name = "pr_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @Column(name = "pr_opened_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime openedDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pr_opened_latitude"))
            , @AttributeOverride(name = "longitude", column = @Column(name = "pr_opened_longitude"))
            , @AttributeOverride(name = "location", column = @Column(name = "pr_opened_location"))
    })
    private Location openedLocation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pr_city_id")
    private City city;

    @PrePersist
    public final void setCreateDate() {
        createdDate = DateTime.now();
    }

    // Getters & setters

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getReport() {
        return report;
    }

    public void setReport(Boolean report) {
        this.report = report;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DateTime getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(DateTime openedDate) {
        this.openedDate = openedDate;
    }

    public Location getOpenedLocation() {
        return openedLocation;
    }

    public void setOpenedLocation(Location openedLocation) {
        this.openedLocation = openedLocation;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

}
