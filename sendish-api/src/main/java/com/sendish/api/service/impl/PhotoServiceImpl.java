package com.sendish.api.service.impl;

import com.sendish.api.dto.*;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.*;
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

    @Autowired
    private PhotoCommentServiceImpl photoCommentService;

    @Autowired
    private RedisStatisticsRepository statisticsRepository;

    private static PrettyTime prettyTime = new PrettyTime();

    public Photo findOne(Long photoId) {
        return photoRepository.findOne(photoId);
    }

    public Long processNewImage(LocationBasedFileUpload p_upload, Long p_userId) {
        DateTime receivedDate = DateTime.now();
        MultipartFile file = p_upload.getImage();
        Photo photo = mapToPhoto(p_upload, p_userId, file);

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
        userService.updateStatisticsForNewSentPhoto(p_userId, receivedDate, location, city);
        // TODO: Distribute new image and create PhotoSendingDetails

        return photo.getId();
    }

    private Photo mapToPhoto(LocationBasedFileUpload p_upload, Long p_userId, MultipartFile file) {
        Photo photo = new Photo();
        photo.setUser(userRepository.findOne(p_userId));
        photo.setName(file.getName());
        photo.setContentType(file.getContentType());
        photo.setSize(file.getSize());
        photo.setDescription(p_upload.getDescription());
        photo.setUuid(UUID.randomUUID().toString());

        return photo;
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
            PhotoDetailsDto photoDetailsDto = new PhotoDetailsDto();
            mapPhotoDetailsDto(photo, photoDetailsDto);

            return photoDetailsDto;
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

        ReceivedPhotoDetailsDto photoDetailsDto = new ReceivedPhotoDetailsDto();
        mapPhotoDetailsDto(photoReceiver.getPhoto(), photoDetailsDto);
        photoDetailsDto.setLike(photoReceiver.getLike());
        photoDetailsDto.setReport(photoReceiver.getReport());

        return photoDetailsDto;
    }

    // TODO: Maybe allow changing dislike to like?
    public void like(Long photoId, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver.getLike() == null) {
            photoReceiver.setLike(true);
            photoReceiverRepository.save(photoReceiver);

            statisticsRepository.likePhoto(photoId, photoReceiver.getPhoto().getUser().getId());
        }
    }

    // TODO: Maybe allow changing like to dislike?
    public void dislike(Long photoId, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver.getLike() == null) {
            photoReceiver.setLike(false);
            photoReceiverRepository.save(photoReceiver);

            statisticsRepository.dislikePhoto(photoId, photoReceiver.getPhoto().getUser().getId());
        }
    }

    public void report(Long photoId, String reason, String reasonText, Long userId) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver.getReport() == null) {
            photoReceiver.setReport(true);
            photoReceiver.setReportType(reason);
            photoReceiver.setReportText(reasonText);
            photoReceiverRepository.save(photoReceiver);

            statisticsRepository.reportPhoto(photoId, photoReceiver.getPhoto().getUser().getId());
        }
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

    private void mapPhotoDetailsDto(Photo photo, PhotoDetailsDto photoDetailsDto) {
        mapToPhotoDto(photo, photoDetailsDto);

        photoDetailsDto.setComments(photoCommentService.findFirstByPhotoId(photo.getId(), 3));
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
        PhotoDto photoDto = new PhotoDto();
        mapToPhotoDto(photo, photoDto);

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
        ReceivedPhotoDto photoDto = new ReceivedPhotoDto();
        mapToPhotoDto(photo.getPhoto(), photoDto);
        photoDto.setLike(photo.getLike());
        photoDto.setReport(photo.getReport());
        photoDto.setOpened(photo.getOpenedDate() != null);

        return photoDto;
    }

    private PhotoDto mapToPhotoDto(Photo photo, PhotoDto photoDto) {
        photoDto.setId(photo.getId());
        photoDto.setOriginLocation(getLocationName(photo.getCity()));
        photoDto.setDescription(photo.getDescription());
        photoDto.setTimeAgo(getPrettyTime(photo.getCreatedDate()));
        photoDto.setUuid(photo.getUuid());

        PhotoStatisticsDto stats = statisticsRepository.getPhotoStatistics(photo.getId());
        photoDto.setCityCount(stats.getCityCount());
        photoDto.setCommentCount(stats.getCommentCount());
        photoDto.setLikeCount(stats.getLikeCount());

        return photoDto;
    }

    private String getPrettyTime(DateTime dateTime) {
        return prettyTime.format(dateTime.toDate());
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
            dto.setLocation(getLocationName(photoReceiver.getCity()));
            dto.setTimeAgo(prettyTime.format(photoReceiver.getCreatedDate().toDate()));
            dto.setId(photoReceiver.getId());

            dtos.add(dto);
        }

        return dtos;
    }

    private String getLocationName(City city) {
        return city.getName() + ", " + city.getCountry().getName();
    }

}
