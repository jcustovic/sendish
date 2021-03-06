package com.sendish.api.distributor.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.service.impl.PhotoSenderServiceImpl;
import com.sendish.repository.PhotoSendingDetailsRepository;

@Component
public class TravelingPhotoResenderScheduler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TravelingPhotoResenderScheduler.class);

	public static final long TWO_MINUTE_DELAY = 120000L;
	public static final long MINUTE_DELAY = 60000L;

    @Autowired
    private PhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Scheduled(fixedDelay = TWO_MINUTE_DELAY, initialDelay = MINUTE_DELAY)
    @Transactional
    public void resendTravelingPhotos() throws InterruptedException {
        LOGGER.info("Starting resend of traveling photos...");
    	Page<Long> photoIds = photoSendingDetailsRepository.findTravelingPhotoIdsByLastSentGreatherThan(new PageRequest(0, 10000, Sort.Direction.DESC, "photoId"));
    	
    	LOGGER.info("Found {} photos that need to be resent.", photoIds.getTotalElements());
        ExecutorService executor = Executors.newFixedThreadPool(20);
    	photoIds.getContent().stream().forEach(p -> executor.execute(() -> photoSenderService.resendPhoto(p, 1)));
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    	LOGGER.info("Sending finished.");
    }

}
