package com.sendish.repository.model.jpa;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "photo_comment")
@SequenceGenerator(name = "idSequence", sequenceName = "photo_comment_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "pc_id"))
public class PhotoComment extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false)
	@JoinColumn(name = "pc_photo_id")
	private Photo photo;

	@ManyToOne(optional = false)
	@JoinColumn(name = "pc_user_id")
	private User user;

	@Column(name = "pc_likes_count", nullable = false)
	private Integer likes = 0;

	@Column(name = "pc_dislikes_count", nullable = false)
	private Integer dislikes = 0;

	@Column(name = "pc_reports_count", nullable = false)
	private Integer reports = 0;

	@Column(name = "pc_deleted", nullable = false)
	private Boolean deleted = false;

	@Column(name = "pc_comment", nullable = false, length = 128)
	private String comment;

	@Column(name = "pc_created_date", nullable = false)
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

	public Integer getLikes() {
		return likes;
	}

	public void setLikes(Integer likes) {
		this.likes = likes;
	}

	public Integer getDislikes() {
		return dislikes;
	}

	public void setDislikes(Integer dislikes) {
		this.dislikes = dislikes;
	}

	public Integer getReports() {
		return reports;
	}

	public void setReports(Integer reports) {
		this.reports = reports;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

}
