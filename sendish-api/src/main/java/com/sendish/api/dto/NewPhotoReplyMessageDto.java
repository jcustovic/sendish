package com.sendish.api.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class NewPhotoReplyMessageDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	private Long photoReplyId;

	@Size(min = 1, max = 1024)
	@NotBlank
	private String message;

	private Long userId;
	
	// Getters & setters

	public Long getPhotoReplyId() {
		return photoReplyId;
	}

	public void setPhotoReplyId(Long photoId) {
		this.photoReplyId = photoId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
