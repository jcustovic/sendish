package com.sendish.api.service.impl;

import com.sendish.api.service.ResizeImageService;
import com.sendish.api.util.RetryUtils;
import com.sendish.repository.model.jpa.ResizedImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("retryableResizeImageService")
public class RetryableResizeImageServiceImpl implements ResizeImageService {

    @Autowired
    private ResizeImageService resizeImageService;

    @Override
    public ResizedImage getResizedImage(Long imageId, String sizeKey) {
        return RetryUtils.retry(() -> {
            return resizeImageService.getResizedImage(imageId, sizeKey);
        }, 3, 50);
    }

}
