package com.sendish.api.dto;

public class ChatMessageDto extends BaseEntityDto {

	private static final long serialVersionUID = 1L;

	public enum ChatMessageDtoType {
		TEXT, IMG
	}

	private ChatMessageDtoType type;
	private String text;
	private String timeAgo;
	private String imageUuid;
	private String displayName;
	
	// Getters & setters
	
	public ChatMessageDtoType getType() {
		return type;
	}
	public void setType(ChatMessageDtoType type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
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

}
