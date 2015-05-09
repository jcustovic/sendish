package com.sendish.repository.model.jpa;

import java.io.Serializable;

public class ChatThreadUserId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long user;
	private Long chatThread;

	public ChatThreadUserId() {
		// Hibernate
	}

	public ChatThreadUserId(Long userId, Long chatThreadId) {
		user = userId;
		chatThread = chatThreadId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((user == null) ? 0 : user.hashCode());
		result = (prime * result) + ((chatThread == null) ? 0 : chatThread.hashCode());

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
		final ChatThreadUserId other = (ChatThreadUserId) obj;
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		if (chatThread == null) {
			if (other.chatThread != null) {
				return false;
			}
		} else if (!chatThread.equals(other.chatThread)) {
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

	public Long getChatThread() {
		return chatThread;
	}

	public void setChatThread(Long chatThread) {
		this.chatThread = chatThread;
	}

}
