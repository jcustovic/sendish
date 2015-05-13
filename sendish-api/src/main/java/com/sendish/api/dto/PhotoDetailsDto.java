package com.sendish.api.dto;

public class PhotoDetailsDto extends CommonPhotoDetailsDto {

	private static final long serialVersionUID = 1L;

	private Boolean like;
	private Boolean forceRating = false;

	public Boolean getLike() {
		return like;
	}

	public void setLike(Boolean like) {
		this.like = like;
	}

	public Boolean getForceRating() {
		return forceRating;
	}

	public void setForceRating(Boolean forceRating) {
		this.forceRating = forceRating;
	}

}
