package com.sendish.repository.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "hot_photo")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class HotPhoto {

	@Id
	@Column(name = "hp_photo_id")
	private Long photoId;

	@OneToOne
	@PrimaryKeyJoinColumn
	private Photo photo;

	@Column(name = "hp_selected_time")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime selectedTime;

	@Column(name = "hp_removed_time")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime removedTime;
	
	// Getters & setters

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public Photo getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}

	public DateTime getSelectedTime() {
		return selectedTime;
	}

	public void setSelectedTime(DateTime selectedTime) {
		this.selectedTime = selectedTime;
	}

	public DateTime getRemovedTime() {
		return removedTime;
	}

	public void setRemovedTime(DateTime removedTime) {
		this.removedTime = removedTime;
	}

}
