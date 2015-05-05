package com.sendish.push.notification;

import java.util.Map;

public interface AsyncNotificationProvider {

    void sendPlainTextNotification(String p_message, Long p_userId);

    void sendPlainTextNotification(String p_message, Map<String, Object> customData, Long p_userId);

    void sendNotificationMessage(Long p_refId, String p_refType, String p_message, Map<String, Object> p_data, Long p_userId);

}
