package com.sendish.repository.model.jpa.listener;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class LocationListener {

    @PrePersist
    public void onPrePersist(Object entity) {
        buildLocation(entity);
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        buildLocation(entity);
    }

    public void buildLocation(Object entity) {
        if (entity instanceof LocationAware) {
            ((LocationAware) entity).buildLocation();
        }
    }

}
