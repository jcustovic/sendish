package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "resized_image")
@SequenceGenerator(name = "idSequence", sequenceName = "resized_image_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "ri_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ResizedImage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ri_image_id", nullable = false)
    private Image image;

    @Column(name = "ri_key", nullable = false)
    private String key;

    @Column(name = "ri_width", nullable = false)
    private Integer width;

    @Column(name = "ri_height", nullable = false)
    private Integer height;

    @Column(name = "ri_size_byte", nullable = false)
    private Long size;

    @Column(name = "ri_storage_id", length = 200, nullable = false, unique = true)
    private String storageId;

    @Column(name = "ri_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = DateTime.now();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

}
