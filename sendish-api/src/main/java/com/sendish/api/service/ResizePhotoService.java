package com.sendish.api.service;

import com.sendish.repository.model.jpa.ResizedPhoto;

public interface ResizePhotoService {

    ResizedPhoto getResizedPhoto(Long photoId, String sizeKey);

}
