package com.sendish.api.dto;

import java.io.Serializable;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class NewCommentDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 1, max = 200)
	@NotBlank
	private String comment;

	private Long replyToId;
	
	// NOTE: Manually populated
	private Long photoId;
	private Long userId;
	
	// Getters & setters

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getReplyToId() {
		return replyToId;
	}

	public void setReplyToId(Long replyToId) {
		this.replyToId = replyToId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
