package com.sendish.api.dto;

public class ChatMessageDto extends BaseEntityDto {

	private static final long serialVersionUID = 1L;

	public enum ChatMessageDtoType {
		TEXT, IMG
	}

	private ChatMessageDtoType type;
	private String text;
	private String time;
	private String url;
	private String username;
	
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
