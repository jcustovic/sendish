package com.sendish.repository.model.jpa;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "photo_comment_vote")
@IdClass(PhotoCommentVoteId.class)
public class PhotoCommentVote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne
    @JoinColumn(name = "pcv_pc_id")
    private PhotoComment comment;

    @Id
    @ManyToOne
    @JoinColumn(name = "pcv_user_id")
    private User user;

    @Column(name = "pcv_like", nullable = false)
    private Boolean like = false;

    @Column(name = "pcv_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = DateTime.now();
    }

    // Getters & setters

    public PhotoComment getComment() {
        return comment;
    }

    public void setComment(PhotoComment comment) {
        this.comment = comment;
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

    public DateTime getCreatedDate() {
        return createdDate;
    }

}
