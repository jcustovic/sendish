package com.sendish.api.service.impl;

import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class PhotoServiceImpl {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoReceiverRepository photoReceiverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityServiceImpl cityService;

    @Autowired
    private FileStore fileStore;

    public Long saveNewImage(LocationBasedFileUpload p_upload, Long p_userId) {
        MultipartFile file = p_upload.getImage();
        Photo photo = new Photo();
        photo.setUser(userRepository.findOne(p_userId));
        photo.setName(file.getName());
        photo.setContentType(file.getContentType());
        photo.setSize(file.getSize());
        photo.setDescription(p_upload.getDescription());
        photo.setResend(true);
        photo.setOriginLocation(new Location(p_upload.getLatitude(), p_upload.getLongitude()));
        photo.setCity(cityService.findNearest(p_upload.getLatitude(), p_upload.getLongitude()));
        photo.setUuid(UUID.randomUUID().toString());

        Dimension dimension;
        try {
            dimension = ImageUtils.getDimension(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read image dimensions for file " + file.getName());
        }
        photo.setWidth((int) dimension.getWidth());
        photo.setHeight((int) dimension.getHeight());

        String fileStoreId;
        try {
            fileStoreId = fileStore.save(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        photo.setStorageId(fileStoreId);

        photo = photoRepository.save(photo);

        return photo.getId();
    }

    public Photo findByUuid(String p_uuid) {
        return photoRepository.findByUuid(p_uuid);
    }

    public InputStream getPhotoContent(String fileStoreId) throws ResourceNotFoundException {
        return fileStore.getAsInputStream(fileStoreId);
    }

    public Photo findReceivedByUuid(String photoUUID, Long userId) {
        return photoReceiverRepository.findPhotoByUserIdAndPhotoUUID(userId, photoUUID);
    }

    public Photo findByUserIdAndUuid(Long userId, String photoUUID) {
        return photoRepository.findByUserIdAndUuid(userId, photoUUID);
    }

}
