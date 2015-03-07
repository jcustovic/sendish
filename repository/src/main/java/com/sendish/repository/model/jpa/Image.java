package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "image")
@SequenceGenerator(name = "idSequence", sequenceName = "image_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "i_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Image extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "i_name", nullable = false, length = 128)
    private String name;

    @Column(name = "i_uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "i_storage_id", length = 200, nullable = false, unique = true)
    private String storageId;

    @Column(name = "i_width", nullable = false)
    private Integer width;

    @Column(name = "i_height", nullable = false)
    private Integer height;

    @Column(name = "i_size_byte", nullable = false)
    private Long size;

    @Column(name = "i_content_type", nullable = false)
    private String contentType;

    @Column(name = "i_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = DateTime.now();
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

}
