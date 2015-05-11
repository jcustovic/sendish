package com.sendish.api.dto;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class ReportPhotoReplyDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	private Long photoReplyId;

	@Size(min = 1, max = 128)
	@NotBlank
	private String reportText;

	@Size(min = 1, max = 32)
	@NotBlank
	private String reportType;

	private Long userId;
	
	// Getters & setters

	public Long getPhotoReplyId() {
		return photoReplyId;
	}

	public void setPhotoReplyId(Long photoId) {
		this.photoReplyId = photoId;
	}

	public String getReportText() {
		return reportText;
	}

	public void setReportText(String reportText) {
		this.reportText = reportText;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
