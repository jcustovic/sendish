package com.sendish.api.distributor.scheduler;

import com.sendish.api.service.impl.PhotoSenderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.PhotoSendingDetailsRepository;
import com.sendish.repository.model.jpa.PhotoSendStatus;
import com.sendish.repository.model.jpa.PhotoStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class NewNotSentPhotoScheduler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NewNotSentPhotoScheduler.class);

    public static final long HALF_MINUTE_DELAY = 30000L;

    @Autowired
    private PhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Scheduled(fixedDelay = HALF_MINUTE_DELAY)
    @Transactional
    public void resendNewUnsentPhotos() throws InterruptedException {
        LOGGER.info("Starting resend of new photos...");
    	Page<Long> photoIds = photoSendingDetailsRepository.findIdsByPhotoStatusAndSendStatus(PhotoStatus.NEW, PhotoSendStatus.NO_USER, new PageRequest(0, 10000));
    	
    	LOGGER.info("Found {} new photos that were not sent immediately.", photoIds.getTotalElements());
        ExecutorService executor = Executors.newFixedThreadPool(20);
        photoIds.getContent().stream().forEach(p -> executor.execute(() -> photoSenderService.resendPhoto(p)));
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    	
    	LOGGER.info("Sending finished.");
    }

}
