package com.sendish.push.notification;

import com.sendish.repository.model.jpa.NotificationPlatformType;

public class NotificationResult {

    private NotificationPlatformType type;
    private Long totalCount;

    public NotificationResult() {
        super();
    }

    public NotificationResult(final NotificationPlatformType p_type, final Long p_totalCount) {
        super();
        type = p_type;
        totalCount = p_totalCount;
    }

    // Getters & setters

    public final NotificationPlatformType getType() {
        return type;
    }

    public final void setType(final NotificationPlatformType p_type) {
        type = p_type;
    }

    public final Long getTotalCount() {
        return totalCount;
    }

    public final void setTotalCount(final Long p_totalCount) {
        totalCount = p_totalCount;
    }

}
