package com.sendish.api.service;

import com.sendish.api.exception.ResizeFailedException;
import com.sendish.repository.model.jpa.ResizedPhoto;

public interface ResizePhotoService {

    ResizedPhoto getResizedPhoto(Long photoId, String sizeKey) throws ResizeFailedException;

}
