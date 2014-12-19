package com.sendish.api.service.impl;

import com.sendish.api.dto.*;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.PhotoStatisticsRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.*;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

@Service
@Transactional
public class PhotoServiceImpl {

    private static final int PHOTO_PAGE_SIZE = 10;
    private static final int PHOTO_LOCATION_PAGE_SIZE = 20;
    
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

    @Autowired
    private UserServiceImpl userService;

    private static PrettyTime prettyTime = new PrettyTime();

    public Photo findOne(Long photoId) {
        return photoRepository.findOne(photoId);
    }

    public Long saveNewImage(LocationBasedFileUpload p_upload, Long p_userId) {
        MultipartFile file = p_upload.getImage();
        Photo photo = new Photo();
        photo.setUser(userRepository.findOne(p_userId));
        photo.setName(file.getName());
        photo.setContentType(file.getContentType());
        photo.setSize(file.getSize());
        photo.setDescription(p_upload.getDescription());
        photo.setResend(true);
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

        Location location = new Location(p_upload.getLatitude(), p_upload.getLongitude());
        City city = cityService.findNearest(p_upload.getLatitude(), p_upload.getLongitude());
        photo.setOriginLocation(location);
        photo.setCity(city);

        photo = photoRepository.save(photo);
        createPhotoStatistics(photo);
        userService.updateLocation(p_userId, location, city);

        return photo.getId();
    }

    private void createPhotoStatistics(Photo photo) {
        PhotoStatistics photoStatistics = new PhotoStatistics();
        photoStatistics.setPhotoId(photo.getId());
        photoStatisticsRepository.save(photoStatistics);
    }

    public InputStream getPhotoContent(String fileStoreId) throws ResourceNotFoundException {
        return fileStore.getAsInputStream(fileStoreId);
    }

    public Photo findReceivedByPhotoUuid(String photoUUID, Long userId) {
        return photoReceiverRepository.findPhotoByUserIdAndPhotoUUID(userId, photoUUID);
    }

    public Photo findByUserIdAndUuid(Long userId, String photoUUID) {
        return photoRepository.findByUserIdAndUuid(userId, photoUUID);
    }

    public List<PhotoDto> findByUserId(Long userId, Integer page) {
        List<Photo> photos = photoRepository.findByUserId(userId, 
        		new PageRequest(page, PHOTO_PAGE_SIZE, Direction.DESC, "createdDate"));

        return getPhotoDtos(photos);
    }

    public List<ReceivedPhotoDto> findReceivedByUserId(Long userId, Integer page) {
        List<PhotoReceiver> photos = photoReceiverRepository.findByUserId(userId, 
        		new PageRequest(page, PHOTO_PAGE_SIZE, Direction.DESC, "createdDate"));

        return getReceivedPhotoDtos(photos);
    }

    public PhotoDetailsDto findByIdAndUserId(Long photoId, Long userId) {
        Photo photo = photoRepository.findByIdAndUserId(photoId, userId);
        if (photo != null) {
            return getPhotoDetailsDto(photo);
        }

        return null;
    }

    public PhotoReceiver findReceivedByPhotoIdAndUserId(Long photoId, Long userId) {
        return photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
    }

    public ReceivedPhotoDetailsDto findReceivedByPhotoIdAndUserId(Long photoId, Long userId, BigDecimal longitude, BigDecimal latitude) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver == null) {
            return null;
        }

        if (photoReceiver.getOpenedDate() == null) {
            photoReceiver.setOpenedDate(DateTime.now());
            Location location = getUserLocation(userId, longitude, latitude);
            photoReceiver.setOpenedLocation(location);
            photoReceiver.setCity(cityService.findNearest(location.getLatitude(), location.getLongitude()));
            photoReceiverRepository.save(photoReceiver);
        }

        // TODO: Mapping
        return new ReceivedPhotoDetailsDto();
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

    public List<PhotoTraveledDto> getTraveledLocations(Long photoId, Integer page) {
        List<PhotoReceiver> receivedList = photoReceiverRepository.findByPhotoId(photoId, new PageRequest(page, PHOTO_LOCATION_PAGE_SIZE, Direction.DESC, "createdDate"));

        return getPhotoTraveledDtos(receivedList);
    }

    public void deletePhotoReceiver(Long photoId, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        photoReceiver.setDeleted(true);

        photoReceiverRepository.save(photoReceiver);
    }

    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findOne(photoId);
        photo.setDeleted(true);

        photoRepository.save(photo);
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
        PhotoDto photoDto = new PhotoDto();
        mapToPhotoDto(photoDto, photo, photoStatistics);

        return photoDto;
    }

    private List<ReceivedPhotoDto> getReceivedPhotoDtos(List<PhotoReceiver> photos) {
        if (photos.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<ReceivedPhotoDto> photoDtos = new ArrayList<>(photos.size());
        for (PhotoReceiver photo : photos) {
            photoDtos.add(getReceiverPhotoDto(photo));
        }

        return photoDtos;
    }

    private ReceivedPhotoDto getReceiverPhotoDto(PhotoReceiver photo) {
        PhotoStatistics photoStatistics = photoStatisticsRepository.findOne(photo.getId());
        ReceivedPhotoDto photoDto = new ReceivedPhotoDto();
        mapToPhotoDto(photoDto, photo.getPhoto(), photoStatistics);
        photoDto.setLike(photo.getLike());
        photoDto.setReport(photo.getReport());
        photoDto.setOpened(photo.getOpenedDate() != null);

        return photoDto;
    }

    private PhotoDto mapToPhotoDto(PhotoDto photoDto, Photo photo, PhotoStatistics photoStatistics) {
        photoDto.setId(photo.getId());
        photoDto.setDescription(photo.getDescription());
        photoDto.setTimeAgo(prettyTime.format(photo.getCreatedDate().toDate()));
        photoDto.setCity(getLocationName(photo));
        photoDto.setImgUuid(photo.getUuid());
        photoDto.setCityCount(photoStatistics.getCities());
        photoDto.setCommentCount(photoStatistics.getComments());
        photoDto.setLikeCount(photoStatistics.getLikes());

        return photoDto;
    }

    private Location getUserLocation(Long userId, BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            return userService.getLastLocation(userId);
        } else {
            userService.updateLocation(userId, longitude, latitude);
            return new Location(latitude, longitude);
        }
    }

    private List<PhotoTraveledDto> getPhotoTraveledDtos(List<PhotoReceiver> receivedList) {
        List<PhotoTraveledDto> dtos = new ArrayList<>(receivedList.size());
        for (PhotoReceiver photoReceiver : receivedList) {
            PhotoTraveledDto dto = new PhotoTraveledDto();
            dto.setLiked(photoReceiver.getLike());
            dto.setLocation(getLocationName(photoReceiver.getPhoto()));
            dto.setTimeAgo(prettyTime.format(photoReceiver.getCreatedDate().toDate()));
            dto.setId(photoReceiver.getId());

            dtos.add(dto);
        }

        return dtos;
    }

    private String getLocationName(Photo photo) {
        return photo.getCity().getName() + ", " + photo.getCity().getCountry().getName();
    }

}
