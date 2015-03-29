package com.sendish.api.distributor.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.service.impl.AsyncPhotoSenderServiceImpl;
import com.sendish.repository.PhotoSendingDetailsRepository;
import com.sendish.repository.model.jpa.PhotoSendStatus;
import com.sendish.repository.model.jpa.PhotoStatus;

@Component
public class NewNotSentPhotoScheduler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NewNotSentPhotoScheduler.class);

    public static final long HALF_MINUTE_DELAY = 30000L;
    
    @Autowired
    private AsyncPhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Scheduled(fixedDelay = HALF_MINUTE_DELAY)
    @Transactional
    public void resendNewUnsentPhotos() {
    	Page<Long> photoIds = photoSendingDetailsRepository.findIdsByPhotoStatusAndSendStatus(PhotoStatus.NEW, PhotoSendStatus.NO_USER, new PageRequest(0, 10000));
    	
    	LOGGER.info("Found {} new photos that were not sent immediately.", photoIds.getTotalElements());
    	
    	photoIds.getContent().stream().forEach(p -> photoSenderService.resendPhoto(p));
    	
    	LOGGER.info("Sending finished.");
    }

}
