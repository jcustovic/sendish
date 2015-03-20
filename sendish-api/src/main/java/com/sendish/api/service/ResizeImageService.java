package com.sendish.api.service;

import com.sendish.repository.model.jpa.ResizedImage;

public interface ResizeImageService {

    ResizedImage getResizedImage(Long imageId, String sizeKey);

}
