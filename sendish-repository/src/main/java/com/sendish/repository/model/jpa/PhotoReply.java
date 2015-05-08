package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "photo_reply")
@SequenceGenerator(name = "idSequence", sequenceName = "photo_reply_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "prp_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PhotoReply extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "pr_uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "pr_name", nullable = false, length = 128)
    private String name;

    @Column(name = "pr_description", length = 200)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pr_photo_id", nullable = false)
    private Photo photo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pr_user_id", nullable = false)
    private User user;

    @Column(name = "pr_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "pr_storage_id", length = 200, nullable = false, unique = true)
    private String storageId;

    @Column(name = "pr_width", nullable = false)
    private Integer width;

    @Column(name = "pr_height", nullable = false)
    private Integer height;

    @Column(name = "pr_size_byte", nullable = false)
    private Long size;

    @Column(name = "pr_content_type", nullable = false)
    private String contentType;

    @Column(name = "pr_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @Column(name = "pr_report_type", length = 32)
    private String reportType;

    @Column(name = "pr_report_text", length = 128)
    private String reportText;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pr_report_by", nullable = false)
    private User reportedBy;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
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

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

}
