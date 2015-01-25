package com.sendish.api.redis.dto;

import java.io.Serializable;

public class UserStatisticsDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long rank;
    private long totalLikeCount;
    private long totalDislikeCount;
    private long totalReportCount;
    private long totalCityCount;
    private long unseenPhotoCount;
    private long dailySendCount;

    public UserStatisticsDto(long totalLikeCount, long totalDislikeCount, long totalReportCount, long dailySendCount) {
        this.totalLikeCount = totalLikeCount;
        this.totalDislikeCount = totalDislikeCount;
        this.totalReportCount = totalReportCount;
        this.dailySendCount = dailySendCount;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public long getTotalLikeCount() {
        return totalLikeCount;
    }

    public void setTotalLikeCount(long totalLikeCount) {
        this.totalLikeCount = totalLikeCount;
    }

    public long getTotalDislikeCount() {
        return totalDislikeCount;
    }

    public void setTotalDislikeCount(long totalDislikeCount) {
        this.totalDislikeCount = totalDislikeCount;
    }

    public long getTotalReportCount() {
        return totalReportCount;
    }

    public void setTotalReportCount(long totalReportCount) {
        this.totalReportCount = totalReportCount;
    }

    public long getTotalCityCount() {
        return totalCityCount;
    }

    public void setTotalCityCount(long totalCityCount) {
        this.totalCityCount = totalCityCount;
    }

    public long getUnseenPhotoCount() {
        return unseenPhotoCount;
    }

    public void setUnseenPhotoCount(long unseenPhotoCount) {
        this.unseenPhotoCount = unseenPhotoCount;
    }

    public long getDailySendCount() {
        return dailySendCount;
    }

    public void setDailySendCount(long dailySendCount) {
        this.dailySendCount = dailySendCount;
    }

}
