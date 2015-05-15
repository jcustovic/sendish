package com.sendish.api;

import com.google.android.gcm.server.Sender;
import com.sendish.push.notification.apns.ApnsNotificationProviderImpl;
import com.sendish.push.notification.DelegateNotificationProvider;
import com.sendish.push.notification.apns.ApnsNotificationSender;
import com.sendish.push.notification.gcm.GcmNotificationProviderImpl;
import com.sendish.push.notification.gcm.GcmNotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan("com.sendish.push.notification")
public class PushNotificationConfig {

    @Value("${app.gcm.api_key}")
    private String gcmApiKey;

    // APNS Setup

    @Bean
    public Executor apnsNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.initialize();

        return executor;
    }

    @Bean
    public ApnsNotificationSender apnsNotificationSender() {
        return new ApnsNotificationSender();
    }

    @Bean
    public ApnsNotificationProviderImpl apnsNotificationProvider() {
        return new ApnsNotificationProviderImpl(apnsNotificationExecutor());
    }

    // GCM Setup

    @Bean
    public Executor gcmNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.initialize();

        return executor;
    }

    @Bean
    public GcmNotificationSender gcmNotificationSender() {
        Sender sender = new Sender(gcmApiKey);

        return new GcmNotificationSender(sender);
    }

    @Bean
    public GcmNotificationProviderImpl gcmNotificationProvider() {
        return new GcmNotificationProviderImpl(gcmNotificationExecutor());
    }

    // Other

    @Bean
    public DelegateNotificationProvider delegateNotificationProvider() {
        return new DelegateNotificationProvider();
    }

}
