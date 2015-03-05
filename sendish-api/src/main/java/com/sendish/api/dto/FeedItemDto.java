package com.sendish.api.dto;

import java.io.Serializable;

public class FeedItemDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;
	private String timeAgo;
	private String photoUuid;
	private Long photoId;
	private PhotoType photoType;
	
	// Getters & setters

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTimeAgo() {
		return timeAgo;
	}

	public void setTimeAgo(String timeAgo) {
		this.timeAgo = timeAgo;
	}

	public String getPhotoUuid() {
		return photoUuid;
	}

	public void setPhotoUuid(String photoUuid) {
		this.photoUuid = photoUuid;
	}

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public PhotoType getPhotoType() {
		return photoType;
	}

	public void setPhotoType(PhotoType photoType) {
		this.photoType = photoType;
	}

}
