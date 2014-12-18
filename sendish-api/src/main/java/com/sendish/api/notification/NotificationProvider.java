package com.sendish.api.notification;

import com.sendish.repository.model.jpa.NotificationMessage;

import java.util.Map;

public interface NotificationProvider {

    NotificationResult pushNotificationMessage(NotificationMessage p_notification, String p_text, Map<String, Object> p_customData, JpaNotificationQueryHolder p_queryHolder);

    NotificationResult pushPlainTextMessage(String p_text, Map<String, Object> p_customData, JpaNotificationQueryHolder p_queryHolder);

}
