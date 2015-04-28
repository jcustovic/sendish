package com.sendish.api.dto;

public class HotPhotoDetailsDto extends PhotoDetailsDto {

	private static final long serialVersionUID = 1L;

	private Boolean like;
	private Boolean backButton = true;

	public Boolean getLike() {
		return like;
	}

	public void setLike(Boolean like) {
		this.like = like;
	}

	public Boolean getBackButton() {
		return backButton;
	}

	public void setBackButton(Boolean backButton) {
		this.backButton = backButton;
	}

}
