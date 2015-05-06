package com.sendish.batch.notification;

import com.sendish.push.notification.ApnsNotificationProvider;
import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.UserDetails;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ComeBackReminderSender {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private ApnsNotificationProvider apnsNotificationProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(ApnsPushToken apnsPushToken) {
        apnsNotificationProvider.sendMessage(apnsPushToken, "We miss you! Check out new photos on hot list!");

        UserDetails userDetails = apnsPushToken.getUser().getDetails();
        userDetails.setComeBackReminderTime(DateTime.now());
        userDetailsRepository.save(userDetails);
    }

}
