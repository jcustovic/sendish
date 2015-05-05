package com.sendish.repository.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "photo_vote")
@IdClass(PhotoVoteId.class)
public class PhotoVote implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pv_photo_id")
	private Photo photo;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pv_user_id")
	private User user;

	@Column(name = "pv_like")
	private Boolean like;

	@Column(name = "pv_report")
	private Boolean report;

	@Column(name = "pv_report_type", length = 32)
	private String reportType;

	@Column(name = "pv_report_text", length = 128)
	private String reportText;

	@Column(name = "pv_created_date", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdDate;

	@PrePersist
	public final void markCreatedDate() {
		createdDate = DateTime.now();
	}

	// Getters & setters

	public Photo getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

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

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getReportText() {
		return reportText;
	}

	public void setReportText(String reportText) {
		this.reportText = reportText;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

}
