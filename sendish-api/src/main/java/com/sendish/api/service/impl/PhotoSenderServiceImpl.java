package com.sendish.api.service.impl;

import java.util.List;

import com.sendish.api.distributor.PhotoDistributor;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoSendingDetailsRepository;
import com.sendish.repository.model.jpa.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PhotoSenderServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PhotoSenderServiceImpl.class);

    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Autowired
    private PhotoDistributor photoDistributor;
    
    @Autowired
    private PhotoReceiverRepository photoReceiverRepository;

    public PhotoSendingDetails sendNewPhoto(Long photoId) {
        PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
        if (photoSendingDetails != null) {
            throw new IllegalStateException("Photo with id " + photoId + " is not a new photo. New photos don't have PhotoSendingDetails entry.");
        }

        photoSendingDetails = new PhotoSendingDetails();
        photoSendingDetails.setPhotoId(photoId);

        List<PhotoReceiver> receivers = photoDistributor.sendNewPhoto(photoId);
        if (receivers.isEmpty()) {
            photoSendingDetails.setPhotoStatus(PhotoStatus.NEW);
            photoSendingDetails.setSendStatus(PhotoSendStatus.NO_USER);
        } else {
            photoSendingDetails.setPhotoStatus(PhotoStatus.TRAVELING);
            photoSendingDetails.setSendStatus(PhotoSendStatus.SENT);
        }

        return photoSendingDetailsRepository.save(photoSendingDetails);
    }

    public void resendPhoto(Long photoId, int numOfRecipients) {
        PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
        if (photoSendingDetails == null) {
            throw new IllegalStateException("PhotoSending with id " + photoId + " not found. Either the photo is not found or it is a new photo.");
        } else if (PhotoStatus.STOPPED.equals(photoSendingDetails.getPhotoStatus())) {
        	LOGGER.debug("Photo {} won't be resent because status is STOPPED", photoId);
        	return;
        }

        List<PhotoReceiver> receivers = photoDistributor.resendPhoto(photoId, numOfRecipients);
        if (receivers.isEmpty()) {
            photoSendingDetails.setSendStatus(PhotoSendStatus.NO_USER);
        } else {
        	photoSendingDetails.setPhotoStatus(PhotoStatus.TRAVELING);
            photoSendingDetails.setSendStatus(PhotoSendStatus.SENT);
        }
        
        // TODO: Multi-thread! Maybe record time when last user received photo?
        photoSendingDetailsRepository.save(photoSendingDetails);
    }

	public void resendPhotoOnLike(Long photoId) {
		PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
		if (photoSendingDetails == null || PhotoStatus.STOPPED.equals(photoSendingDetails.getPhotoStatus())) {
			LOGGER.debug("PhotoSendingDetails not found or photo is STOPPED. It is probably auto sender photo!", photoId);
			return;
		}
		
		resendPhoto(photoId, 1);
	}

	public void stopSending(Long photoId, String stopReason) {
        LOGGER.debug("Stopping photo with id {}", photoId);
		PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
		if (photoSendingDetails != null && !PhotoStatus.STOPPED.equals(photoSendingDetails.getPhotoStatus())) {
			photoSendingDetails.setPhotoStatus(PhotoStatus.STOPPED);
			photoSendingDetails.setPhotoStatusReason(stopReason);
			
			photoSendingDetailsRepository.save(photoSendingDetails);
			
			photoReceiverRepository.deleteUnopenedByPhotoId(photoId);
		}
	}

}
