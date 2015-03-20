package com.sendish.api.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class UserSettingsDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Min(5)
	@Max(100)
	private Integer receiveLimitPerDay;

	@NotNull
	private Boolean receiveNewPhotoNotifications;

	@NotNull
	private Boolean receiveCommentNotifications;

    @Size(max = 20, min = 2)
    @NotEmpty
	private String nickname;

	// Getters & setters

	public Integer getReceiveLimitPerDay() {
		return receiveLimitPerDay;
	}

	public void setReceiveLimitPerDay(Integer receiveLimitPerDay) {
		this.receiveLimitPerDay = receiveLimitPerDay;
	}

	public Boolean getReceiveNewPhotoNotifications() {
		return receiveNewPhotoNotifications;
	}

	public void setReceiveNewPhotoNotifications(Boolean receiveNewPhotoNotifications) {
		this.receiveNewPhotoNotifications = receiveNewPhotoNotifications;
	}

	public Boolean getReceiveCommentNotifications() {
		return receiveCommentNotifications;
	}

	public void setReceiveCommentNotifications(Boolean receiveCommentNotifications) {
		this.receiveCommentNotifications = receiveCommentNotifications;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
