package com.sendish.api.redis.dto;

import java.io.Serializable;

public class PhotoStatisticsDto implements Serializable {

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

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(long dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public long getCityCount() {
        return cityCount;
    }

    public void setCityCount(long cityCount) {
        this.cityCount = cityCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

}
