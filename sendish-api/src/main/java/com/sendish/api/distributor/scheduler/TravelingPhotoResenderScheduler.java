package com.sendish.api.distributor.scheduler;

import org.joda.time.DateTime;
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

@Component
public class TravelingPhotoResenderScheduler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TravelingPhotoResenderScheduler.class);

	public static final long ONE_MINUTE_DELAY = 60000L;
    
    @Autowired
    private AsyncPhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Scheduled(fixedDelay = ONE_MINUTE_DELAY)
    @Transactional
    public void resendTravelingPhotos() {
    	Page<Long> photoIds = photoSendingDetailsRepository.findTravelingPhotoIdsByLastSentGreatherThan(DateTime.now().minusMinutes(15), new PageRequest(0, 1000));
    	
    	LOGGER.info("Found {} photos that need to be resent.", photoIds.getTotalElements());
    	
    	photoIds.getContent().stream().forEach(p -> photoSenderService.resendPhoto(p));
    }

}
