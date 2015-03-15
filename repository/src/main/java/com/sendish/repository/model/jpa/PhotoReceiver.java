package com.sendish.repository.model.jpa;

import com.sendish.repository.model.jpa.listener.LocationListener;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "photo_receiver")
@SequenceGenerator(name = "idSequence", sequenceName = "photo_receiver_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "pr_id"))
@EntityListeners(LocationListener.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PhotoReceiver extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false)
	@JoinColumn(name = "pr_photo_id")
	@Fetch(FetchMode.JOIN)
	private Photo photo;

	@ManyToOne(optional = false)
	@JoinColumn(name = "pr_user_id")
	private User user;

	@Column(name = "pr_deleted", nullable = false)
	private Boolean deleted = false;

	@Column(name = "pr_created_date", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdDate;

	@Column(name = "pr_opened_date")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime openedDate;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "latitude", column = @Column(name = "pr_opened_latitude")),
			@AttributeOverride(name = "longitude", column = @Column(name = "pr_opened_longitude")),
			@AttributeOverride(name = "location", column = @Column(name = "pr_opened_location")) })
	private Location openedLocation;

	@ManyToOne
	@JoinColumn(name = "pr_city_id")
	private City city;

	@PrePersist
	public void setCreateDate() {
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}

	public DateTime getOpenedDate() {
		return openedDate;
	}

	public void setOpenedDate(DateTime openedDate) {
		this.openedDate = openedDate;
	}

	public Location getOpenedLocation() {
		return openedLocation;
	}

	public void setOpenedLocation(Location openedLocation) {
		this.openedLocation = openedLocation;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

}
