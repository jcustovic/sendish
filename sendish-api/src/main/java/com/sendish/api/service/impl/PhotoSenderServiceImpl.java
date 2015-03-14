package com.sendish.api.service.impl;

import com.sendish.api.distributor.PhotoDistributor;
import com.sendish.repository.PhotoRepository;
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
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Autowired
    private PhotoDistributor photoDistributor;

    public PhotoSendingDetails sendNewPhoto(Long photoId) {
        PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
        if (photoSendingDetails != null) {
            throw new IllegalStateException("Photo with id " + photoId + " is not a new photo. New photos don't have PhotoSendingDetails entry.");
        }

        photoSendingDetails = new PhotoSendingDetails();
        photoSendingDetails.setPhotoId(photoId);

        PhotoReceiver result = photoDistributor.sendPhoto(photoId);
        if (result == null) {
            photoSendingDetails.setPhotoStatus(PhotoStatus.NEW);
            photoSendingDetails.setSendStatus(PhotoSendStatus.NO_USER);
        } else {
            photoSendingDetails.setPhotoStatus(PhotoStatus.TRAVELING);
            photoSendingDetails.setSendStatus(PhotoSendStatus.SENT);
            photoSendingDetails.setLastReceiver(result);
        }

        return photoSendingDetailsRepository.save(photoSendingDetails);
    }

    public void resendPhoto(Long photoId) {
        PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
        if (photoSendingDetails == null) {
            throw new IllegalStateException("PhotoSending with id " + photoId + " not found. Either the photo is not found or it is a new photo.");
        } else if (PhotoStatus.STOPPED.equals(photoSendingDetails.getPhotoStatus())) {
        	LOGGER.debug("Photo {} won't be resent because status is STOPPED", photoId);
        	return;
        }

        PhotoReceiver result = photoDistributor.sendPhoto(photoId);
        if (result == null) {
            photoSendingDetails.setSendStatus(PhotoSendStatus.NO_USER);
        } else {
        	photoSendingDetails.setPhotoStatus(PhotoStatus.TRAVELING);
            photoSendingDetails.setSendStatus(PhotoSendStatus.SENT);
            photoSendingDetails.setLastReceiver(result);
        }
    }

	public void resendPhotoOnLike(Long photoId, Long photoReceiverId) {
		PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
		Long lastPhotoReceiverId = photoSendingDetails.getLastReceiver().getId();
		
		if (lastPhotoReceiverId.equals(photoReceiverId)) {
			resendPhoto(photoId);
		} else {
			LOGGER.debug("Not resending because photo receiver ({}) is not the last receiver ({})", photoReceiverId, lastPhotoReceiverId);
		}
	}

}
