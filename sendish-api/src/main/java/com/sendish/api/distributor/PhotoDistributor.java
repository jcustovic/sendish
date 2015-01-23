package com.sendish.api.distributor;

import java.util.List;

public interface PhotoDistributor {

    void sendPhoto(Long photoId);

    void sendPhoto(List<Long> photoIds);

}
