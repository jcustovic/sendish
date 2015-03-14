package com.sendish.repository.model.jpa;

import java.io.Serializable;

public class PhotoVoteId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long user;
	private Long photo;

	public PhotoVoteId() {
		// Hibernate
	}

	public PhotoVoteId(Long userId, Long photoId) {
		user = userId;
		photo = photoId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((user == null) ? 0 : user.hashCode());
		result = (prime * result) + ((photo == null) ? 0 : photo.hashCode());

		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PhotoVoteId other = (PhotoVoteId) obj;
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		if (photo == null) {
			if (other.photo != null) {
				return false;
			}
		} else if (!photo.equals(other.photo)) {
			return false;
		}

		return true;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	public Long getPhoto() {
		return photo;
	}

	public void setPhoto(Long photo) {
		this.photo = photo;
	}

}
