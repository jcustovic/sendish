package com.sendish.api.redis.dto;

import java.io.Serializable;

public class UserStatisticsDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long totalLikeCount;
	private Long totalDislikeCount;
	private Long totalReportCount;
	private Long totalCityCount;
	private Long unseenPhotoCount;
	private Long unreadInboxItemCount;
	private Long dailySendCount;
	private boolean hasNewActivities;

	public UserStatisticsDto(Long totalLikeCount, Long totalDislikeCount,
			Long totalReportCount, Long dailySendCount, Long unseenPhotoCount,
			Long unreadInboxItemCount, Long totalCityCount,
			Boolean hasNewActivities) {
		this.totalLikeCount = totalLikeCount;
		this.totalDislikeCount = totalDislikeCount;
		this.totalReportCount = totalReportCount;
		this.dailySendCount = dailySendCount;
		this.unseenPhotoCount = unseenPhotoCount;
		this.unreadInboxItemCount = unreadInboxItemCount;
		this.totalCityCount = totalCityCount;
		this.hasNewActivities = hasNewActivities;
	}

	public Long getTotalLikeCount() {
		return totalLikeCount;
	}

	public Long getTotalDislikeCount() {
		return totalDislikeCount;
	}

	public Long getTotalReportCount() {
		return totalReportCount;
	}

	public Long getTotalCityCount() {
		return totalCityCount;
	}

	public Long getUnseenPhotoCount() {
		return unseenPhotoCount;
	}

	public Long getUnreadInboxItemCount() {
		return unreadInboxItemCount;
	}

	public Long getDailySendCount() {
		return dailySendCount;
	}

	public boolean getHasNewActivities() {
		return hasNewActivities;
	}

}
