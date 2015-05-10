package com.sendish.api.dto;

import java.util.List;

public class ChatThreadDetailsDto extends BaseEntityDto {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private List<ChatMessageDto> messages;
	
	// Getters & setters

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ChatMessageDto> getMessages() {
		return messages;
	}

	public void setMessages(List<ChatMessageDto> messages) {
		this.messages = messages;
	}

}
