package com.sendish.repository.model.jpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("GCM")
public class GcmPushToken extends PushToken {

    private static final long serialVersionUID = 1L;

    public GcmPushToken() {
        super();
    }

    public GcmPushToken(final String p_token, final User p_user) {
        super(p_token, p_user);
    }

}
