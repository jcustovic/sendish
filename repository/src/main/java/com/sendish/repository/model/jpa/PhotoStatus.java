package com.sendish.repository.model.jpa;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "photo_status")
public class PhotoStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ps_photo_id")
    private Long photoId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Photo photo;

    @Column(name = "ps_resend_stopped", nullable = false)
    private Boolean resendStopped = false;

    @ManyToOne
    @JoinColumn(name = "ps_last_photo_rec_id")
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

    public Boolean getResendStopped() {
        return resendStopped;
    }

    public void setResendStopped(Boolean resendStopped) {
        this.resendStopped = resendStopped;
    }

    public PhotoReceiver getLastReceiver() {
        return lastReceiver;
    }

    public void setLastReceiver(PhotoReceiver lastReceiver) {
        this.lastReceiver = lastReceiver;
    }

}
