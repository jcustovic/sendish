package com.sendish.api.dto;

public class ReceivedPhotoDetailsDto extends PhotoDetailsDto {

	private static final long serialVersionUID = 1L;

	private Boolean like;
	private Boolean report;
	private Boolean opened;
	private Boolean backButton = true;

	public Boolean getLike() {
		return like;
	}

	public void setLike(Boolean like) {
		this.like = like;
	}

	public Boolean getReport() {
		return report;
	}

	public void setReport(Boolean report) {
		this.report = report;
	}

	public Boolean getOpened() {
		return opened;
	}

	public void setOpened(Boolean opened) {
		this.opened = opened;
	}

	public Boolean getBackButton() {
		return backButton;
	}

	public void setBackButton(Boolean backButton) {
		this.backButton = backButton;
	}

}
