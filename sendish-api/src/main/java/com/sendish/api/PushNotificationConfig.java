package com.sendish.api;

import com.sendish.push.notification.ApnsNotificationProviderImpl;
import com.sendish.push.notification.DelegateNotificationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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

    @DependsOn(value = "apnsNotificationExecutor")
    @Bean
    public ApnsNotificationProviderImpl apnsNotificationProvider() {
        return new ApnsNotificationProviderImpl();
    }

    @Bean
    public DelegateNotificationProvider delegateNotificationProvider() {
        return new DelegateNotificationProvider();
    }

}
