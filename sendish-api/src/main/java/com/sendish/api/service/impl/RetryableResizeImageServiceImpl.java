package com.sendish.api.service.impl;

import com.sendish.api.exception.ResizeFailedException;
import com.sendish.api.exception.UnsupportedResizeKey;
import com.sendish.api.service.ResizeImageService;
import com.sendish.repository.model.jpa.ResizedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service("retryableResizeImageService")
public class RetryableResizeImageServiceImpl implements ResizeImageService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RetryableResizeImageServiceImpl.class);

    @Autowired
    private ResizeImageService resizeImageService;

    @Override
    public ResizedImage getResizedImage(Long imageId, String sizeKey) throws ResizeFailedException {
        try {
	    	try {
	    		return resizeImageService.getResizedImage(imageId, sizeKey);
	    	} catch (DataIntegrityViolationException e) {
	    		LOGGER.info("Image {} resize attempt failed (Error: {}). Retrying...", imageId, e.getMessage());
	    		// Multiple request at the same time can do a resize. The second time it should work!
	    		return resizeImageService.getResizedImage(imageId, sizeKey);
	    	}
    	} catch (UnsupportedResizeKey e) {
    		LOGGER.warn("Resize image " + imageId + " for key " + sizeKey + " failed exception", e);
    		throw new ResizeFailedException(e);
    	} catch (Exception e) {
    		LOGGER.error("Resize photo " + imageId + " for key " + sizeKey + " failed exception", e);
    		throw new ResizeFailedException(e);
    	}
    }

}
