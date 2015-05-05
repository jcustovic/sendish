package com.sendish.repository.model.jpa;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "push_notification_token")
@SequenceGenerator(name = "idSequence", sequenceName = "push_notification_token_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name="pt_id"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "pt_platform_type")
public abstract class PushToken extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "pt_token", length = 200, nullable = false, unique = true)
    private String            token;

    @Column(name = "pt_modified_date", nullable = false)
    private Date              modifiedDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pt_user_id", nullable = false)
    private User              user;

    @Column(name = "pt_dev_token", nullable = false)
    private Boolean devToken = false;

    public PushToken() {
        super();
    }

    public PushToken(final String p_token, final User p_user) {
        super();
        token = p_token;
        user = p_user;
    }

    @PrePersist
    @PreUpdate
    public final void updateModifyDate() {
        modifiedDate = new Date();
    }

    // Getters & setters

    public final String getToken() {
        return token;
    }

    public final void setToken(final String p_token) {
        token = p_token;
    }

    public final Date getModifiedDate() {
        return modifiedDate;
    }

    public final void setModifiedDate(final Date p_modifiedDate) {
        modifiedDate = p_modifiedDate;
    }

    public final User getUser() {
        return user;
    }

    public final void setUser(final User p_user) {
        user = p_user;
    }

    public Boolean isDevToken() {
        return devToken;
    }

    public void setDevToken(Boolean devToken) {
        this.devToken = devToken;
    }

}
