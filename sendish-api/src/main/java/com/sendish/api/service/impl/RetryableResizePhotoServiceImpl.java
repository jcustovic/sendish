package com.sendish.api.service.impl;

import com.sendish.api.service.ResizePhotoService;
import com.sendish.api.util.RetryUtils;
import com.sendish.repository.model.jpa.ResizedPhoto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("retryableResizePhotoService")
public class RetryableResizePhotoServiceImpl implements ResizePhotoService {

    @Autowired
    private ResizePhotoService resizePhotoService;

    @Override
    public ResizedPhoto getResizedPhoto(Long photoId, String sizeKey) {
        return RetryUtils.retry(() -> {
            return resizePhotoService.getResizedPhoto(photoId, sizeKey);
        }, 3, 50);
    }

}
