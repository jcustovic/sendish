package com.sendish.api.notification;

import com.sendish.repository.model.jpa.NotificationMessage;

import java.util.Map;

public interface NotificationProvider {

    NotificationResult notify(NotificationMessage p_notification, String p_text, Map<String, Object> p_data, JpaNotificationQueryHolder p_notificationQueryHolder);

    NotificationResult notifyByToken(NotificationMessage p_notification, String p_text, Map<String, Object> p_data, String p_token, boolean p_devToken);

}
