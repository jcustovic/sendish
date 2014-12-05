package com.sendish.api.service.impl;

import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.dto.PhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.PhotoStatisticsRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;
import com.sendish.repository.model.jpa.PhotoStatistics;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Service
public class PhotoServiceImpl {

    private static final int PHOTO_PAGE_SIZE = 10;
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

    @Autowired
    private PhotoStatisticsRepository photoStatisticsRepository;

    private static PrettyTime prettyTime = new PrettyTime();

    @Transactional
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
        createPhotoStatistics(photo);

        return photo.getId();
    }

    private void createPhotoStatistics(Photo photo) {
        PhotoStatistics photoStatistics = new PhotoStatistics();
        photoStatistics.setPhotoId(photo.getId());
        photoStatisticsRepository.save(photoStatistics);
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

    public List<PhotoDto> findByUserId(Long userId, Integer page) {
        List<Photo> photos = photoRepository.findByUserId(userId, new PageRequest(page, PHOTO_PAGE_SIZE));

        return getPhotoDtos(photos);
    }

    public List<PhotoDto> findReceivedByUserId(Long userId, Integer page) {
        List<Photo> photos = photoReceiverRepository.findPhotosByUserId(userId, new PageRequest(page, PHOTO_PAGE_SIZE));

        return getPhotoDtos(photos);
    }

    public PhotoDetailsDto findByIdAndUserId(Long photoId, Long userId) {
        Photo photo = photoRepository.findByIdAndUserId(photoId, userId);
        if (photo != null) {
            return getPhotoDetailsDto(photo);
        }

        return null;
    }

    public PhotoDetailsDto findReceivedByIdAndUserId(Long photoId, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver != null) {
            // TODO: Mapping
            return new PhotoDetailsDto();
        }

        return null;
    }

    public void like(Long photoId, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        photoReceiver.setLike(true);
        // TODO: Counting

        photoReceiverRepository.save(photoReceiver);
    }

    public void dislike(Long photoId, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        photoReceiver.setLike(false);
        // TODO: Counting

        photoReceiverRepository.save(photoReceiver);
    }

    public void report(Long photoId, String reason, String reasonText, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        photoReceiver.setReport(true);
        photoReceiver.setReportType(reason);
        photoReceiver.setReportText(reasonText);
        // TODO: Counting

        photoReceiverRepository.save(photoReceiver);
    }

    private PhotoDetailsDto getPhotoDetailsDto(Photo photo) {
        PhotoDetailsDto detailsDto = new PhotoDetailsDto();
        detailsDto.setId(photo.getId());
        // TODO: Mapping

        return detailsDto;
    }

    private List<PhotoDto> getPhotoDtos(List<Photo> photos) {
        if (photos.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<PhotoDto> photoDtos = new ArrayList<>(photos.size());
        for (Photo photo : photos) {
            photoDtos.add(getPhotoDto(photo));
        }

        return photoDtos;
    }

    private PhotoDto getPhotoDto(Photo photo) {
        PhotoStatistics photoStatistics = photoStatisticsRepository.findOne(photo.getId());

        return mapToPhotoDto(photo, photoStatistics);
    }

    private PhotoDto mapToPhotoDto(Photo photo, PhotoStatistics photoStatistics) {
        PhotoDto photoDto = new PhotoDto();
        photoDto.setId(photo.getId());
        photoDto.setDescription(photo.getDescription());
        photoDto.setTimeAgo(prettyTime.format(photo.getCreatedDate().toDate()));
        photoDto.setCity(photo.getCity().getName() + ", " + photo.getCity().getCountry().getName());
        photoDto.setImgUuid(photo.getUuid());
        photoDto.setCityCount(photoStatistics.getCities());
        photoDto.setCommentCount(photoStatistics.getComments());
        photoDto.setLikeCount(photoStatistics.getLikes());

        return photoDto;
    }

}
