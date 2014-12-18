package com.sendish.api.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class LocationDto {

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

}
