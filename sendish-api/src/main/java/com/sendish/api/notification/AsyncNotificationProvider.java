package com.sendish.api.notification;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface AsyncNotificationProvider {

    @Async
    void sendPlainTextNotification(String p_message, Long p_userId);

    void notifyAsync(Long p_refId, String p_refType, String p_message, Map<String, Object> p_data, Long p_userId);

}
