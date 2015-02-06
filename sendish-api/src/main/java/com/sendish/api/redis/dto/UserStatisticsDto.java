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

    public UserStatisticsDto(long totalLikeCount, long totalDislikeCount, long totalReportCount, long dailySendCount, Long unseenPhotoCount) {
        this.totalLikeCount = totalLikeCount;
        this.totalDislikeCount = totalDislikeCount;
        this.totalReportCount = totalReportCount;
        this.dailySendCount = dailySendCount;
        this.unseenPhotoCount = unseenPhotoCount;
    }

    public long getRank() {
        return rank;
    }

    public long getTotalLikeCount() {
        return totalLikeCount;
    }

    public long getTotalDislikeCount() {
        return totalDislikeCount;
    }

    public long getTotalReportCount() {
        return totalReportCount;
    }

    public long getTotalCityCount() {
        return totalCityCount;
    }

    public long getUnseenPhotoCount() {
        return unseenPhotoCount;
    }

    public long getDailySendCount() {
        return dailySendCount;
    }

}
