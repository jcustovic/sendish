package com.sendish.api.service.impl;

import java.awt.Dimension;
import java.io.IOException;
import java.util.UUID;

import com.sendish.api.store.FileStore;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.ImageRepository;
import com.sendish.repository.model.jpa.Image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImageServiceImpl {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FileStore fileStore;
    
    public Image findByUuid(String imageUUID) {
        return imageRepository.findByUuid(imageUUID);
    }

    public Image create(MultipartFile imageFile) {
        Image image = new Image();
        image.setContentType(imageFile.getContentType());
        image.setSize(imageFile.getSize());
        image.setName(imageFile.getName());

        Dimension dimension;
        try {
            dimension = ImageUtils.getDimension(imageFile.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read image dimensions for file " + imageFile.getName());
        }
        image.setWidth((int) dimension.getWidth());
        image.setHeight((int) dimension.getHeight());

        String fileStoreId;
        try {
            fileStoreId = fileStore.save(imageFile.getInputStream(), "image_original");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        image.setStorageId(fileStoreId);
        image.setUuid(UUID.randomUUID().toString());

        return imageRepository.save(image);
    }

    public Image findOne(Long imageId) {
        return imageRepository.findOne(imageId);
    }

}
