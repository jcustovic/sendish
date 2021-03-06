package com.sendish.repository.model.jpa;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "user_activity")
@SequenceGenerator(name = "idSequence", sequenceName = "user_activity_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "ua_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserActivity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "ua_user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ua_from_user_id")
	private User fromUser;

	@Column(name = "ua_text", nullable = false, length = 512)
	private String text;

	@Column(name = "ua_created_date", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdDate;

	@Column(name = "ua_image_uuid", length = 36)
	private String imageUuid;

	@Column(name = "ua_reference_type", length = 32, nullable = false)
	private String referenceType;

	@Column(name = "ua_reference_id", length = 32)
	private String referenceId;

	@PrePersist
	public final void markCreatedDate() {
		createdDate = DateTime.now();
	}

	// Getters & setters

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getFromUser() {
		return fromUser;
	}

	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

	public String getImageUuid() {
		return imageUuid;
	}

	public void setImageUuid(String imageUuid) {
		this.imageUuid = imageUuid;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

}
