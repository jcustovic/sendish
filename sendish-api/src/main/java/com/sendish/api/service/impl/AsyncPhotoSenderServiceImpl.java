package com.sendish.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncPhotoSenderServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncPhotoSenderServiceImpl.class);

    @Autowired
    private PhotoSenderServiceImpl photoSenderService;

    @Async
    public void resendPhoto(Long photoId) {
        try {
            photoSenderService.resendPhoto(photoId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Async
	public void resendPhotoOnLike(Long photoId, Long photoReceiverId) {
		try {
            photoSenderService.resendPhotoOnLike(photoId, photoReceiverId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
	}

}
