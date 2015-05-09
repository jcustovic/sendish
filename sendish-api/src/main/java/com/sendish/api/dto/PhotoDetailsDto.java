package com.sendish.api.dto;

import java.util.List;

public class PhotoDetailsDto extends PhotoDto {

	private static final long serialVersionUID = 1L;

	private List<CommentDto> comments;
	private Long photoReplyId;

	// Getters & setters

	public List<CommentDto> getComments() {
		return comments;
	}

	public void setComments(List<CommentDto> comments) {
		this.comments = comments;
	}

	public Long getPhotoReplyId() {
		return photoReplyId;
	}

	public void setPhotoReplyId(Long photoReplyId) {
		this.photoReplyId = photoReplyId;
	}

}
