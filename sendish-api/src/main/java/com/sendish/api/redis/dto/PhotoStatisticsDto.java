package com.sendish.api.redis.dto;

import java.io.Serializable;

public class PhotoStatisticsDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long openedCount;
	private Long likeCount;
	private Long dislikeCount;
	private Long cityCount;
	private Long commentCount;
	private Long reportCount;

	public PhotoStatisticsDto(Long openedCount, Long likeCount, Long dislikeCount, Long cityCount, Long commentCount, Long reportCount) {
		this.openedCount = openedCount;
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.cityCount = cityCount;
		this.commentCount = commentCount;
		this.reportCount = reportCount;
	}

	public Long getOpenedCount() {
		return openedCount;
	}

	public void setOpenedCount(Long openedCount) {
		this.openedCount = openedCount;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public Long getDislikeCount() {
		return dislikeCount;
	}

	public Long getCityCount() {
		return cityCount;
	}

	public Long getCommentCount() {
		return commentCount;
	}

	public Long getReportCount() {
		return reportCount;
	}

}
