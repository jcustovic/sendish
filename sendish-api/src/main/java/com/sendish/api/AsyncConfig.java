package com.sendish.api;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurerSupport {

	@Override
	public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor scheduler = new ThreadPoolTaskExecutor();
        scheduler.setCorePoolSize(5);
        scheduler.setMaxPoolSize(30);
        scheduler.setThreadNamePrefix("async-task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        scheduler.initialize();

        return scheduler;
	}

}