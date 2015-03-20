package com.sendish.api.photo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.sendish.repository.HotPhotoRepository;
import com.sendish.repository.model.jpa.HotPhoto;

@Component
public class HotPhotoDecider {

	private static final int MIN_LIKES_COUNT_FOR_HOT_LIST = 30;
	
	@Autowired
	private HotPhotoRepository hotPhotoRepository;

	@Async
	public void decide(Long photoId, Long likeCount) {
		if (likeCount > MIN_LIKES_COUNT_FOR_HOT_LIST) {
			HotPhoto hotPhoto = hotPhotoRepository.findOne(photoId);
			if (hotPhoto == null) {
				// TODO: Send email that new photo is nominated for photo list	
			}
		}
	}

}
