package com.sendish.api.distributor.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sendish.api.service.impl.AsyncPhotoSenderServiceImpl;
import com.sendish.repository.PhotoSendingDetailsRepository;
import com.sendish.repository.model.jpa.PhotoSendStatus;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.PhotoStatus;

@Component
public class NewNotSentPhotoScheduler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NewNotSentPhotoScheduler.class);

    public static final long TEN_SECONDS_DELAY = 10000L;
    
    @Autowired
    private AsyncPhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Scheduled(fixedDelay = TEN_SECONDS_DELAY)
    public void sendNewUnsentPhotos() {
    	Page<PhotoSendingDetails> photoDetails = photoSendingDetailsRepository.findByPhotoStatusAndSendStatus(PhotoStatus.NEW, PhotoSendStatus.NO_USER, new PageRequest(0, 1000));
    	
    	LOGGER.info("Found {} new photos that were not sent immediately.", photoDetails.getTotalElements());
    	
    	for (PhotoSendingDetails photoSendingDetails : photoDetails) {
    		photoSenderService.resendPhoto(photoSendingDetails.getPhotoId());
    	}
    }

}
