package com.sendish.api.redis.dto;

public class PhotoStatDto {

    private long likeCounter;
    private long dislikeCounter;
    private long cityCounter;
    private long commentCounter;

    public PhotoStatDto(long likeCounter, long dislikeCounter, long cityCounter, long commentCounter) {
        this.likeCounter = likeCounter;
        this.dislikeCounter = dislikeCounter;
        this.cityCounter = cityCounter;
    }

    public long getLikeCounter() {
        return likeCounter;
    }

    public void setLikeCounter(long likeCounter) {
        this.likeCounter = likeCounter;
    }

    public long getDislikeCounter() {
        return dislikeCounter;
    }

    public void setDislikeCounter(long dislikeCounter) {
        this.dislikeCounter = dislikeCounter;
    }

    public long getCityCounter() {
        return cityCounter;
    }

    public void setCityCounter(long cityCounter) {
        this.cityCounter = cityCounter;
    }

    public long getCommentCounter() {
        return commentCounter;
    }

    public void setCommentCounter(long commentCounter) {
        this.commentCounter = commentCounter;
    }

}
