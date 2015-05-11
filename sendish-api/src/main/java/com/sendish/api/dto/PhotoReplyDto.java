package com.sendish.api.dto;

public class PhotoReplyDto extends BaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String lastActivity;
	private String imgUuid;
	private String username;
	private String text;
	private Boolean received;

	// Getters & setters

	public String getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(String lastActivity) {
		this.lastActivity = lastActivity;
	}

	public String getImgUuid() {
		return imgUuid;
	}

	public void setImgUuid(String imgUuid) {
		this.imgUuid = imgUuid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Boolean getReceived() {
		return received;
	}

	public void setReceived(Boolean received) {
		this.received = received;
	}

}
