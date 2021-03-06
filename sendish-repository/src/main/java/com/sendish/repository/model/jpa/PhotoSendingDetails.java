package com.sendish.repository.model.jpa;

import javax.persistence.*;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;

@Entity
@Table(name = "photo_sending_details")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PhotoSendingDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "psd_photo_id")
    private Long photoId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Photo photo;

    @Enumerated(EnumType.STRING)
    @Column(name = "psd_photo_status")
    private PhotoStatus photoStatus;
    
    @Column(name = "psd_photo_status_reason", length = 32)
    private String photoStatusReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "psd_send_status")
    private PhotoSendStatus sendStatus;

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

    public PhotoStatus getPhotoStatus() {
        return photoStatus;
    }

    public void setPhotoStatus(PhotoStatus photoStatus) {
        this.photoStatus = photoStatus;
    }
    
    public String getPhotoStatusReason() {
		return photoStatusReason;
	}

	public void setPhotoStatusReason(String photoStatusReason) {
		this.photoStatusReason = photoStatusReason;
	}

	public PhotoSendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(PhotoSendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

}
