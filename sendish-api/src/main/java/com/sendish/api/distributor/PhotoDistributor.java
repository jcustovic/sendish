package com.sendish.api.distributor;

import java.util.List;

import com.sendish.repository.model.jpa.PhotoReceiver;

public interface PhotoDistributor {

    List<PhotoReceiver> resendPhoto(Long photoId);

	List<PhotoReceiver> sendNewPhoto(Long photoId);

}
