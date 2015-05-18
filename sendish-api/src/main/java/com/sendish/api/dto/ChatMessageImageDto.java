package com.sendish.api.dto;

import java.io.Serializable;

import com.sendish.repository.model.jpa.ChatMessageType;

public class ChatMessageImageDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String uuid;
	private ChatMessageType type;
	private String relativePath;
	private Integer width;
	private Integer height;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public ChatMessageType getType() {
		return type;
	}

	public void setType(ChatMessageType type) {
		this.type = type;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

}
