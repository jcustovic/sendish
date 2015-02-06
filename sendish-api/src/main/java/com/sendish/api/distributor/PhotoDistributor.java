package com.sendish.api.distributor;

import com.sendish.repository.model.jpa.PhotoReceiver;

public interface PhotoDistributor {

    PhotoReceiver sendPhoto(Long photoId);

}
