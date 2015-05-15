package com.sendish.push.notification.apns;

import com.notnoop.apns.ApnsService;
import com.sendish.repository.NotificationMessageRepository;
import com.sendish.repository.NotificationPartialResultRepository;
import com.sendish.repository.model.jpa.NotificationMessage;
import com.sendish.repository.model.jpa.NotificationPartialResult;
import com.sendish.repository.model.jpa.NotificationPlatformType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApnsNotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(ApnsNotificationSender.class);

    private static final int TWELVE_HOUR_EXPIRY = 1200;

    private static final int POOL_SIZE = 10;

    @Autowired
    private NotificationPartialResultRepository notificationPartialResultRepository;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    @Autowired
    private transient ErrorApnsDelegate apnsDelegate;

    @Value("${app.ios.cert.prod.path:}")
    private Resource certificatePath;

    @Value("${app.ios.cert.prod.pass:}")
    private String certificatePassword;

    @Value("${app.ios.cert.dev.path:}")
    private Resource devCertificatePath;

    @Value("${app.ios.cert.dev.pass:}")
    private String devCertificatePassword;

    private transient ApnsService prodService;
    private transient ApnsService devService;

    @PostConstruct
    public void setup() throws IOException {
        if (certificatePath != null && certificatePath.exists()) {
            LOG.info("Configuring prod certificate service...");
            prodService = new JKSApnsServiceBuilder() //
                    .withCert(certificatePath.getInputStream(), certificatePassword) //
                    .withProductionDestination() //
                    .asPool(POOL_SIZE) //
                    .withDelegate(apnsDelegate) //
                    .build();
        }

        if (devCertificatePath != null && devCertificatePath.exists()) {
            LOG.info("Configuring dev certificate service...");
            devService = new JKSApnsServiceBuilder() //
                    .withCert(devCertificatePath.getInputStream(), devCertificatePassword) //
                    .withSandboxDestination() //
                    .withDelegate(apnsDelegate) //
                    .build();
        }
    }

    @Transactional
    public void sendPushMessageAndCollectResults(String message, List<String> refIds, Long notificationMsgId) {
        sendPushMessageAndCollectResults(prodService, message, refIds, notificationMsgId);
    }

    @Transactional
    public void sendDevPushMessageAndCollectResults(String message, List<String> refIds, Long notificationMsgId) {
        sendPushMessageAndCollectResults(devService, message, refIds, notificationMsgId);
    }

    public void sendPushMessage(String message, List<String> refIds) {
        sendPushMessage(prodService, message, refIds);
    }

    public void sendDevPushMessage(String message, List<String> refIds) {
        sendPushMessage(devService, message, refIds);
    }

    public Map<String, Date> getInactiveDevices() {
        if (prodService == null) {
            return new HashMap<>();
        } else {
            return prodService.getInactiveDevices();
        }
    }

    public Map<String, Date> getDevInactiveDevices() {
        if (devService == null) {
            return new HashMap<>();
        } else {
            return devService.getInactiveDevices();
        }
    }

    private void sendPushMessageAndCollectResults(ApnsService service, String message, List<String> refIds, Long notificationMsgId) {
        NotificationMessage notification = notificationMessageRepository.findOne(notificationMsgId);
        NotificationPartialResult notificationResult = new NotificationPartialResult();
        notificationResult.setNotification(notification);
        notificationResult.setPlatformType(NotificationPlatformType.APNS);
        notificationResult.setSendDate(DateTime.now());
        notificationResult.setTotalCount(refIds.size());

        sendPushMessage(service, message, refIds);

        notificationResult.setResponseDate(DateTime.now());
        notificationResult.setFailureCount(0); // We cannot know the failure count
        notificationPartialResultRepository.save(notificationResult);
    }

    private void sendPushMessage(ApnsService service, String message, List<String> refIds) {
        Date expiryDate = DateTime.now().plusMinutes(TWELVE_HOUR_EXPIRY).toDate(); // Expire in 60min
        service.push(refIds, message, expiryDate);

        LOG.trace("Notification sent --> count: {}", refIds.size());
    }

}
