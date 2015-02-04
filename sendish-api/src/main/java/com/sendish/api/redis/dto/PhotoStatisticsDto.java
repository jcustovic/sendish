package com.sendish.api.redis.dto;

import java.io.Serializable;

public class PhotoStatisticsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long likeCount;
    private long dislikeCount;
    private long cityCount;
    private long commentCount;

    public PhotoStatisticsDto(long likeCount, long dislikeCount, long cityCount, long commentCount) {
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.cityCount = cityCount;
        this.commentCount = commentCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getDislikeCount() {
        return dislikeCount;
    }

    public long getCityCount() {
        return cityCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

}
