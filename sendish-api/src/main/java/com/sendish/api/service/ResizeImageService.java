package com.sendish.api.service;

import com.sendish.api.exception.ResizeFailedException;
import com.sendish.repository.model.jpa.ResizedImage;

public interface ResizeImageService {

    ResizedImage getResizedImage(Long imageId, String sizeKey) throws ResizeFailedException;

}
