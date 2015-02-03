package com.sendish.api.redis.dto;

import java.io.Serializable;

public class CommentStatisticsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long likeCount;
    private long dislikeCount;

    public CommentStatisticsDto(long likeCount, long dislikeCount) {
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getDislikeCount() {
        return dislikeCount;
    }

}
