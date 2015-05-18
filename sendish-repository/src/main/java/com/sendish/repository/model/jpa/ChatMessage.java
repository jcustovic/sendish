package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "chat_message")
@SequenceGenerator(name = "idSequence", sequenceName = "chat_message_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "cm_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChatMessage extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cm_thread_id", nullable = false)
	private ChatThread chatThread;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cm_user_id", nullable = false)
	private User user;

    @Enumerated(EnumType.STRING)
	@Column(name = "cm_type", length = 32, nullable = false)
	private ChatMessageType type;

	@Column(name = "cm_uuid", length = 36)
	private String imageUuid;

	@Column(name = "cm_text", length = 1024)
	private String text;

	@Column(name = "cm_deleted", nullable = false)
	private Boolean deleted = false;

	@Column(name = "cm_created_date", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdDate;

	@PrePersist
	public final void markCreatedDate() {
		createdDate = DateTime.now();
	}

	// Getters & setters

	public ChatThread getChatThread() {
		return chatThread;
	}

	public void setChatThread(ChatThread chatThread) {
		this.chatThread = chatThread;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

    public String getImageUuid() {
        return imageUuid;
    }

    public void setImageUuid(String imageUuid) {
        this.imageUuid = imageUuid;
    }

    public ChatMessageType getType() {
        return type;
    }

    public void setType(ChatMessageType type) {
        this.type = type;
    }

    public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

}
