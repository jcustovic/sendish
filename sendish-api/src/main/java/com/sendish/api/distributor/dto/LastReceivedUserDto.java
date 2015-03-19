package com.sendish.api.distributor.dto;

import org.joda.time.DateTime;

public class LastReceivedUserDto {

    private Long userId;
    private DateTime lastReceivedTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public DateTime getLastReceivedTime() {
        return lastReceivedTime;
    }

    public void setLastReceivedTime(DateTime lastReceivedTime) {
        this.lastReceivedTime = lastReceivedTime;
    }

}
