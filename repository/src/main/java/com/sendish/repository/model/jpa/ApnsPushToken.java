package com.sendish.repository.model.jpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("APNS")
public class ApnsPushToken extends PushToken {

    private static final long serialVersionUID = 1L;

    public ApnsPushToken() {
        super();
    }

    public ApnsPushToken(final String p_token, final User p_user) {
        super(p_token, p_user);
    }

}
