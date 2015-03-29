package com.sendish.api.service.impl;

import com.sendish.api.exception.ResizeFailedException;
import com.sendish.api.service.ResizePhotoService;
import com.sendish.repository.model.jpa.ResizedPhoto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service("retryableResizePhotoService")
public class RetryableResizePhotoServiceImpl implements ResizePhotoService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RetryableResizePhotoServiceImpl.class);

    @Autowired
    private ResizePhotoService resizePhotoService;

    @Override
    public ResizedPhoto getResizedPhoto(Long photoId, String sizeKey) throws ResizeFailedException {
    	try {
	    	try {
	    		return resizePhotoService.getResizedPhoto(photoId, sizeKey);
	    	} catch (DataIntegrityViolationException e) {
	    		LOGGER.info("Photo {} resize attempt failed (Error: {}). Retrying...", photoId, e.getMessage());
	    		// Multiple request at the same time can do a resize. The second time it should work!
	    		return resizePhotoService.getResizedPhoto(photoId, sizeKey);
	    	}
    	} catch (Exception e) {
    		LOGGER.error("Resize photo " + photoId + " for key " + sizeKey + " failed exception", e);
    		throw new ResizeFailedException(e);
    	}
    }

}
