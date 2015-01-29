package com.sendish.api.distributor.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserPoolFillerScheduler {

    public static final long TEN_SECONDS_DELAY = 10000L;

    @Scheduled(fixedDelay = TEN_SECONDS_DELAY)
    public void fillUsers() {
        // UserDetails: (lastInteractionTime not null AND lastInteractionTime > today - 10days) AND (receiveAllowedTime IS NULL OR receiveAllowedTime >= now)
        // AND (lastReceivedTime >= redis (max lastReceivedTime) or lastReceivedTime == null) order by lastReceivedTime ASC
        // User: disabled = false, deleted = false
    }

}
