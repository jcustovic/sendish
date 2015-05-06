package com.sendish.api.photo;

import com.sendish.api.service.MailSenderService;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.PhotoStatisticsRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoStatistics;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.sendish.repository.HotPhotoRepository;
import com.sendish.repository.model.jpa.HotPhoto;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Transactional
public class HotPhotoDecider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotPhotoDecider.class);

	private static final int MIN_LIKES_COUNT_FOR_HOT_LIST = 15;

	@Autowired
	private HotPhotoRepository hotPhotoRepository;

    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private PhotoStatisticsRepository photoStatisticsRepository;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private FileStore fileStore;

	@Async
	public void decide(Long photoId, Long likeCount) {
        try {
            // TODO: Maybe consider greater also and somehow mark that the mail was sent already so we don't spam!
            if (likeCount == MIN_LIKES_COUNT_FOR_HOT_LIST) {
                HotPhoto hotPhoto = hotPhotoRepository.findOne(photoId);
                if (hotPhoto == null) {
                    Photo photo = photoRepository.findOne(photoId);
                    PhotoStatistics photoStat = photoStatisticsRepository.findOne(photoId);
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("photo", photo);
                    variables.put("photoStat", photoStat);
                    variables.put("likeCount", likeCount);
                    // TODO: Description in template and number of dislikes from DB

                    Map<String, byte[]> inlineImages = getInlineImages(photo);

                    try {
                        mailSenderService.sendEmail("info@sendish.com", "Sendish Hot photo <hot-photo@sendish.com>", "New hot photo!", variables, "new-hot-photo", inlineImages);
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
        }
	}

    private Map<String, byte[]> getInlineImages(Photo photo) throws ResourceNotFoundException, IOException {
        Map<String, byte[]> images = new HashMap<>();
        images.put("hotPhotoImage", getPhotoAsBytes(photo.getStorageId()));

        return images;
    }

    private byte[] getPhotoAsBytes(String storageId) throws ResourceNotFoundException, IOException {
        return IOUtils.toByteArray(fileStore.getAsInputStream(storageId));
    }

}
