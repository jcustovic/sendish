package com.sendish.repository.model.jpa;

import java.io.Serializable;

public class PhotoCommentVoteId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long user;
    private Long comment;

    public PhotoCommentVoteId() {
        // Hibernate
    }

    public PhotoCommentVoteId(Long userId, Long photoCommentId) {
        user = userId;
        comment = photoCommentId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((user == null) ? 0 : user.hashCode());
        result = (prime * result) + ((comment == null) ? 0 : comment.hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhotoCommentVoteId other = (PhotoCommentVoteId) obj;
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }

        return true;
    }


    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Long getComment() {
        return comment;
    }

    public void setComment(Long comment) {
        this.comment = comment;
    }

}
