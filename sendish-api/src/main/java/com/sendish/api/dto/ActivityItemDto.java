package com.sendish.api.dto;

import java.io.Serializable;

public class ActivityItemDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String displayName;
	private String description;
	private String timeAgo;
	private String photoUuid;
	private String referenceId;
	private String referenceType;

	// Getters & setters

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

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

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

}
