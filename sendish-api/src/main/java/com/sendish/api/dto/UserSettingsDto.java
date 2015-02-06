package com.sendish.api.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class UserSettingsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Min(5)
    @Max(100)
    private Integer receiveLimitPerDay;

    @NotNull
    private Boolean receiveNotifications;

    // Getters & setters

    public Integer getReceiveLimitPerDay() {
        return receiveLimitPerDay;
    }

    public void setReceiveLimitPerDay(Integer receiveLimitPerDay) {
        this.receiveLimitPerDay = receiveLimitPerDay;
    }

    public Boolean getReceiveNotifications() {
        return receiveNotifications;
    }

    public void setReceiveNotifications(Boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }

}
