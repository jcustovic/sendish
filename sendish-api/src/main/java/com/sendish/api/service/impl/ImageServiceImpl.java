package com.sendish.api.service.impl;

import com.sendish.repository.ImageRepository;
import com.sendish.repository.model.jpa.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl {

    @Autowired
    private ImageRepository imageRepository;
    
    public Image findByUuid(String imageUUID) {
        return imageRepository.findByUuid(imageUUID);
    }

}
