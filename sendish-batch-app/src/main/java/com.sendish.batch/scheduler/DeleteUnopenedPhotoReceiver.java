package com.sendish.batch.scheduler;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.PhotoReceiverRepository;

@Component
public class DeleteUnopenedPhotoReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUnopenedPhotoReceiver.class);

    private static final int FIVE_MIN_RATE = 300000;

    @Autowired
    private PhotoReceiverRepository photoReceiverRepository;

    @Scheduled(fixedDelay = FIVE_MIN_RATE)
    @Transactional
    public void checkForInvalidTokens() {
        LOG.debug("Started delete uopened photo receiver scheduler");

        DateTime olderThan = DateTime.now().minusDays(2);
        Integer deleteCount = photoReceiverRepository.deleteUnopenedOlderThan(olderThan);
        
        LOG.debug("Unopened photo receiver finished and deleted {} unopened photos older than {}", deleteCount, olderThan);
    }

}
