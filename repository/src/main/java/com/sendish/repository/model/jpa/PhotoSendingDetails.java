package com.sendish.repository.model.jpa;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "photo_sending_details")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "psd_send_status")
    private PhotoSendStatus sendStatus;

    @ManyToOne
    @JoinColumn(name = "psd_last_photo_rec_id")
    private PhotoReceiver lastReceiver;

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

    public PhotoSendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(PhotoSendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public PhotoReceiver getLastReceiver() {
        return lastReceiver;
    }

    public void setLastReceiver(PhotoReceiver lastReceiver) {
        this.lastReceiver = lastReceiver;
    }

}