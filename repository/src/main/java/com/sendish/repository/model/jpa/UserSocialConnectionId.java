package com.sendish.repository.model.jpa;

import java.io.Serializable;

public class UserSocialConnectionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long   userId;
    private String providerId;
    private String providerUserId;

    public UserSocialConnectionId() {
        super();
    }

    public UserSocialConnectionId(final Long p_userId, final String p_providerId, final String p_providerUserId) {
        super();
        userId = p_userId;
        providerId = p_providerId;
        providerUserId = p_providerUserId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((providerId == null) ? 0 : providerId.hashCode());
        result = (prime * result) + ((providerUserId == null) ? 0 : providerUserId.hashCode());
        result = (prime * result) + ((userId == null) ? 0 : userId.hashCode());
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
        final UserSocialConnectionId other = (UserSocialConnectionId) obj;
        if (providerId == null) {
            if (other.providerId != null) {
                return false;
            }
        } else if (!providerId.equals(other.providerId)) {
            return false;
        }
        if (providerUserId == null) {
            if (other.providerUserId != null) {
                return false;
            }
        } else if (!providerUserId.equals(other.providerUserId)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    // Getters & setters

    public final Long getUserId() {
        return userId;
    }

    public final void setUserId(final Long p_userId) {
        userId = p_userId;
    }

    public final String getProviderId() {
        return providerId;
    }

    public final void setProviderId(final String p_providerId) {
        providerId = p_providerId;
    }

    public final String getProviderUserId() {
        return providerUserId;
    }

    public final void setProviderUserId(final String p_providerUserId) {
        providerUserId = p_providerUserId;
    }

}
