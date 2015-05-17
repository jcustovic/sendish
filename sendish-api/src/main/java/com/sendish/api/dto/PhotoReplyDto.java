package com.sendish.api.dto;

public class PhotoReplyDto extends BaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String timeAgo;
	private String imageUuid;
	private String displayName;
	private String message;
	private Boolean received;

	// Getters & setters

	public String getTimeAgo() {
		return timeAgo;
	}

	public void setTimeAgo(String timeAgo) {
		this.timeAgo = timeAgo;
	}

	public String getImageUuid() {
		return imageUuid;
	}

	public void setImageUuid(String imageUuid) {
		this.imageUuid = imageUuid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Boolean getReceived() {
		return received;
	}

	public void setReceived(Boolean received) {
		this.received = received;
	}

}
