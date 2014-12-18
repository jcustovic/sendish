package com.sendish.api;

import com.sendish.api.notification.ApnsNotificationProvider;
import com.sendish.api.notification.DelegateNotificationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class PushNotificationConfig {

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
    public ApnsNotificationProvider apnsNotificationProvider() {
        return new ApnsNotificationProvider();
    }

    @Bean
    public DelegateNotificationProvider delegateNotificationProvider() {
        return new DelegateNotificationProvider();
    }

}
