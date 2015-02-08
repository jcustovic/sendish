package com.sendish.api.service.impl;

import com.sendish.api.distributor.PhotoDistributor;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.PhotoSendingDetailsRepository;
import com.sendish.repository.model.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class PhotoSenderServiceImpl {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoSendingDetailsRepository photoSendingDetailsRepository;

    @Autowired
    private PhotoDistributor photoDistributor;

    public void sendNewPhoto(Long photoId) {
        PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
        if (photoSendingDetails != null) {
            throw new IllegalStateException("Photo with id " + photoId + " is not a new photo. New photos don't have PhotoSendingDetails entry.");
        }

        Photo photo = photoRepository.findOne(photoId);
        photoSendingDetails = new PhotoSendingDetails();
        photoSendingDetails.setPhotoId(photo.getId());

        PhotoReceiver result = photoDistributor.sendPhoto(photoId);
        if (result == null) {
            photoSendingDetails.setPhotoStatus(PhotoStatus.NEW);
            photoSendingDetails.setSendStatus(PhotoSendStatus.NO_USER);
        } else {
            photoSendingDetails.setPhotoStatus(PhotoStatus.TRAVELING);
            photoSendingDetails.setSendStatus(PhotoSendStatus.SENT);
            photoSendingDetails.setLastReceiver(result);
        }

        photoSendingDetailsRepository.save(photoSendingDetails);
    }

    public void resendPhoto(Long photoId) {
        PhotoSendingDetails photoSendingDetails = photoSendingDetailsRepository.findOne(photoId);
        if (photoSendingDetails == null) {
            throw new IllegalStateException("PhotoSending with id " + photoId + " not found. Either the photo is not found or it is a new photo.");
        }

        PhotoReceiver result = photoDistributor.sendPhoto(photoId);
        if (result == null) {
            photoSendingDetails.setSendStatus(PhotoSendStatus.NO_USER);
        } else {
            photoSendingDetails.setSendStatus(PhotoSendStatus.SENT);
            photoSendingDetails.setLastReceiver(result);
        }
    }

}
