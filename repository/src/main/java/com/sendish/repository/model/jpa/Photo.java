package com.sendish.repository.model.jpa;

import com.sendish.repository.model.jpa.listener.LocationAware;
import com.sendish.repository.model.jpa.listener.LocationListener;
import org.hibernate.annotations.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "photo")
@SequenceGenerator(name = "idSequence", sequenceName = "photo_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "p_id"))
@EntityListeners(LocationListener.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Photo extends BaseEntity implements LocationAware {

    private static final long serialVersionUID = 1L;

    @Column(name = "p_name", nullable = false, length = 128)
    private String name;

    @Column(name = "p_uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "p_description", length = 200)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "p_user_id")
    private User user;

    @Column(name = "p_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "p_sender_deleted", nullable = false)
    private Boolean senderDeleted = false;

    @Column(name = "p_storage_id", length = 200, nullable = false, unique = true)
    private String storageId;

    @Column(name = "p_width", nullable = false)
    private Integer width;

    @Column(name = "p_height", nullable = false)
    private Integer height;

    @Column(name = "p_size_byte", nullable = false)
    private Long size;

    @Column(name = "p_content_type", nullable = false)
    private String contentType;

    @Column(name = "p_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "p_origin_latitude", nullable = false))
            , @AttributeOverride(name = "longitude", column = @Column(name = "p_origin_longitude", nullable = false))
            , @AttributeOverride(name = "location", column = @Column(name = "p_origin_location", nullable = false))
    })
    private Location originLocation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "p_city_id")
    private City city;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = DateTime.now();
    }

    @Override
    public void buildLocation() {
        if (originLocation != null) {
            originLocation.buildLocation();
        }
    }

    // Getters & setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getSenderDeleted() {
        return senderDeleted;
    }

    public void setSenderDeleted(Boolean senderDeleted) {
        this.senderDeleted = senderDeleted;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public Location getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(Location originLocation) {
        this.originLocation = originLocation;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

}
