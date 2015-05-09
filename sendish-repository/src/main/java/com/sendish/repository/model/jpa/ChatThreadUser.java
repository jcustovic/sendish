package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "chat_thread_user")
@IdClass(ChatThreadUserId.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChatThreadUser implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "ctu_thread_id", nullable = false)
    private ChatThread chatThread;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "ctu_user_id", nullable = false)
    private User user;

    @Column(name = "ctu_thread_name", length = 64)
    private String threadName;

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

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

}
