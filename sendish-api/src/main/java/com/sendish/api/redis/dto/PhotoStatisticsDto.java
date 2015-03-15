package com.sendish.api.redis.dto;

import java.io.Serializable;

public class PhotoStatisticsDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private long openedCount;
	private long likeCount;
	private long dislikeCount;
	private long cityCount;
	private long commentCount;
	private long reportCount;

	public PhotoStatisticsDto(long openedCount, long likeCount, long dislikeCount, long cityCount, long commentCount, long reportCount) {
		this.openedCount = openedCount;
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.cityCount = cityCount;
		this.commentCount = commentCount;
		this.reportCount = reportCount;
	}

	public long getOpenedCount() {
		return openedCount;
	}

	public void setOpenedCount(long openedCount) {
		this.openedCount = openedCount;
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

	public long getReportCount() {
		return reportCount;
	}

}
