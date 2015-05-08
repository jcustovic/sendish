package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "chat_thread")
@SequenceGenerator(name = "idSequence", sequenceName = "chat_thread_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "cth_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChatThread extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "cth_photo_reply_id", nullable = false)
    private PhotoReply photoReply;

    @Column(name = "cth_last_activity_time", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastActivity;

    @Column(name = "cth_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = lastActivity = DateTime.now();
    }

    // Getters & setters

    public PhotoReply getPhotoReply() {
        return photoReply;
    }

    public void setPhotoReply(PhotoReply photoReply) {
        this.photoReply = photoReply;
    }

    public DateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(DateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

}
