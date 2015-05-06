package com.sendish.batch.scheduler;

import com.sendish.batch.notification.ComeBackReminderSender;
import com.sendish.repository.ApnsPushTokenRepository;
import com.sendish.repository.model.jpa.ApnsPushToken;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
public class ApnsTokensComeBackReminderScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ApnsTokensComeBackReminderScheduler.class);

    private static final int ONE_HOUR_RATE = 3600000;

    @Autowired
    private ApnsPushTokenRepository apnsPushTokenRepository;

    @Autowired
    private ComeBackReminderSender comeBackReminderSender;

    @Transactional(readOnly = true)
    @Scheduled(fixedDelay = ONE_HOUR_RATE)
    public void sendComeBackRemindersForIOSUsers() {
        LOG.debug("Started come back reminder for iOS users");
        DateTime lastInteractionTimeLowerThan = DateTime.now().minusDays(1);
        DateTime lastTimeSentLowerThan = DateTime.now().minusDays(3);
        try (Stream<ApnsPushToken> tokens = apnsPushTokenRepository.streamUsersThatRequiredFirstComeBackMsgToBeSent(lastInteractionTimeLowerThan)) {
            if (tokens != null) {
                final int[] count = {0};
                tokens.forEach(token -> {
                    comeBackReminderSender.sendNotification(token);
                    count[0]++;
                });
                LOG.info("Sent {} first time come back reminders", count[0]);
            }
        }

        try (Stream<ApnsPushToken> tokens = apnsPushTokenRepository.streamUsersThatRequiredRecurringComeBackMsgToBeSent(lastInteractionTimeLowerThan, lastTimeSentLowerThan)) {
            if (tokens != null) {
                final int[] count = {0};
                tokens.forEach(token -> {
                    comeBackReminderSender.sendNotification(token);
                    count[0]++;
                });
                LOG.info("Sent {} recurring come back reminders", count[0]);
            }
        }
        LOG.debug("Finished come back reminder for iOS users");
    }

}
