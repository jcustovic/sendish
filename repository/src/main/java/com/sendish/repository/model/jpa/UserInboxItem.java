package com.sendish.repository.model.jpa;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "user_inbox_item")
@SequenceGenerator(name = "idSequence", sequenceName = "user_inbox_item_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "uii_id"))
public class UserInboxItem extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false)
    @JoinColumn(name = "uii_user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uii_inbox_message_id")
    private InboxMessage inboxMessage;

    @Column(name = "uii_first_opened_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime firstOpenedDate;

    @Column(name = "uii_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "uii_read", nullable = false)
    private Boolean read = false;

    @Column(name = "uii_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

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

    public InboxMessage getInboxMessage() {
        return inboxMessage;
    }

    public void setInboxMessage(InboxMessage inboxMessage) {
        this.inboxMessage = inboxMessage;
    }

    public DateTime getFirstOpenedDate() {
        return firstOpenedDate;
    }

    public void setFirstOpenedDate(DateTime firstOpenedDate) {
        this.firstOpenedDate = firstOpenedDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

}
