package com.sendish.api.service.impl;

import java.awt.Dimension;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.dto.PhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.dto.PhotoTraveledDto;
import com.sendish.api.dto.ReceivedPhotoDetailsDto;
import com.sendish.api.dto.ReceivedPhotoDto;
import com.sendish.api.mapper.PhotoDtoMapper;
import com.sendish.api.notification.AsyncNotificationProvider;
import com.sendish.api.redis.KeyUtils;
import com.sendish.api.store.FileStore;
import com.sendish.api.util.CityUtils;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.PhotoVoteRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.PhotoVote;
import com.sendish.repository.model.jpa.PhotoVoteId;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserDetails;

@Service
@Transactional
public class PhotoServiceImpl {
	
	// private static final Logger LOGGER = LoggerFactory.getLogger(PhotoServiceImpl.class);

    private static final int PHOTO_PAGE_SIZE = 20;
    private static final int PHOTO_LOCATION_PAGE_SIZE = 20;
    private static final int MAX_LOCATION_NAME_LENGTH_PHOTO_DETAILS = 24;
    private static final int MAX_LOCATION_NAME_LENGTH_PHOTO_LIST = 30;
    
    private static PrettyTime prettyTime = new PrettyTime();
    
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
    private PhotoStatisticsServiceImpl photoStatisticsService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PhotoCommentServiceImpl photoCommentService;

    @Autowired
    private StatisticsServiceImpl statisticsService;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private PhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private AsyncNotificationProvider notificationProvider;
    
    @Autowired
    private PhotoDtoMapper photoDtoMapper;
    
    @Autowired
    private RankingServiceImpl rankingService;
    
    @Autowired
    private PhotoVoteRepository photoVoteRepository;

    public Photo findOne(Long photoId) {
        return photoRepository.findOne(photoId);
    }

    public Photo findByUuid(String photoUuid) {
        return photoRepository.findByUuid(photoUuid);
    }

    public PhotoSendingDetails processNewPhoto(LocationBasedFileUpload upload, Long userId) {
        DateTime uploadedDate = DateTime.now();
        MultipartFile file = upload.getImage();
        Photo photo = mapToPhoto(upload, userId, file);

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
            fileStoreId = fileStore.save(file.getInputStream(), "photo_original");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        photo.setStorageId(fileStoreId);

        Location location = new Location(upload.getLatitude(), upload.getLongitude());
        City city = cityService.findNearest(upload.getLatitude(), upload.getLongitude());
        photo.setOriginLocation(location);
        photo.setCity(city);

        photo = photoRepository.save(photo);
        photoStatisticsService.createNew(photo.getId());
        userService.updateUserLocationAndIncreaseDailySentCount(userId, uploadedDate, location, city);
        
        User user = userRepository.findOne(userId);
        rankingService.addPointsForNewSendish(user);
        
        return photoSenderService.sendNewPhoto(photo.getId());
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

        return photoDtoMapper.mapToPhotoDto(photos, MAX_LOCATION_NAME_LENGTH_PHOTO_LIST);
    }

    public List<ReceivedPhotoDto> findAutoReceivedByUserId(Long userId, Integer page) {
        List<PhotoReceiver> photos = photoReceiverRepository.findAutoReceivedByUserId(userId,
                new PageRequest(page, PHOTO_PAGE_SIZE, Direction.DESC, "createdDate"));
        
        if (page == 0) {
			statisticsService.resetUserUnseenCount(userId);
		}

        return mapToReceivedPhotoDto(photos, MAX_LOCATION_NAME_LENGTH_PHOTO_LIST);
    }

    public PhotoDetailsDto findByIdAndUserId(Long photoId, Long userId) {
        Photo photo = photoRepository.findByIdAndUserId(photoId, userId);
        if (photo != null) {
            PhotoDetailsDto photoDetailsDto = new PhotoDetailsDto();
            mapPhotoDetailsDto(photo, photoDetailsDto, userId);

            return photoDetailsDto;
        }

        return null;
    }

    public PhotoReceiver findReceivedByPhotoIdAndUserId(Long photoId, Long userId) {
        return photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
    }

    public ReceivedPhotoDetailsDto getReceivedByPhotoIdAndUserId(Long photoId, Long userId, BigDecimal longitude, BigDecimal latitude) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver == null) {
            return null;
        }

        ReceivedPhotoDetailsDto photoDetailsDto = new ReceivedPhotoDetailsDto();
        if (photoReceiver.getOpenedDate() == null) {
            saveAndMarkReceivedPhotoAsOpened(photoReceiver, longitude, latitude);
        } else {
        	PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
        	if (vote == null && userId == 4) {
        		photoDetailsDto.setForceRating(true);
        	} else if (vote != null) {
        		photoDetailsDto.setLike(vote.getLike());
                photoDetailsDto.setReport(vote.getReport());		
        	}
        }
        
        mapPhotoDetailsDto(photoReceiver.getPhoto(), photoDetailsDto, userId);

        return photoDetailsDto;
    }

    public void saveAndMarkReceivedPhotoAsOpened(PhotoReceiver photoReceiver) {
        saveAndMarkReceivedPhotoAsOpened(photoReceiver, null, null);
    }

    public void saveAndMarkReceivedPhotoAsOpened(PhotoReceiver photoReceiver, BigDecimal longitude, BigDecimal latitude) {
        Long userId = photoReceiver.getUser().getId();
        Long photoId = photoReceiver.getPhoto().getId();
        Location location = getUserLocation(userId, longitude, latitude);
        City city = cityService.findNearest(location.getLatitude(), location.getLongitude());

        photoReceiver.setOpenedDate(DateTime.now());
        photoReceiver.setOpenedLocation(location);
        photoReceiver.setCity(city);
        photoReceiverRepository.save(photoReceiver);

        statisticsService.trackReceivedPhotoOpened(photoId, userId, city.getId());
    }

    public List<PhotoTraveledDto> getTraveledLocations(Long photoId, Integer page) {
        List<PhotoReceiver> receivedList = photoReceiverRepository.findByPhotoIdAndOpenedDateNotNull(photoId, new PageRequest(page, PHOTO_LOCATION_PAGE_SIZE, Direction.DESC, "openedDate"));

        return mapToPhotoTraveledDto(receivedList);
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

    public boolean hasAlreadyReceivedPhoto(Long photoId, Long userId) {
        return usersReceivedPhotos(userId).isMember(photoId.toString());
    }

    public PhotoReceiver sendPhotoToUser(Long photoId, Long userId) {
        Photo photo = photoRepository.findOne(photoId);
        User user = userRepository.findOne(userId);

        PhotoReceiver photoReceiver = new PhotoReceiver();
        photoReceiver.setUser(user);
        photoReceiver.setPhoto(photo);
        photoReceiver.setAutoReceived(true);
        photoReceiverRepository.save(photoReceiver);

        UserDetails userDetails = userService.getUserDetails(userId);
        userDetails.setLastReceivedTime(photoReceiver.getCreatedDate());
        userService.saveUserDetails(userDetails);

        // TODO: Move to separate method after PhotoReceiver has been saved and committed!
        addPhotoToUsersViewedList(userId, photoId);
        statisticsService.incrementUserUnseenPhotoCount(userId);
        statisticsService.increaseUserDailyReceivedPhotoCount(userId, photoReceiver.getCreatedDate().toLocalDate());
        
        if (userDetails.getReceiveNewPhotoNotifications()) {
        	sendNewPhotoNotification(userId, photo);
        }

        return photoReceiver;
    }

    public void addPhotoToUsersViewedList(Long userId, Long photoId) {
        usersReceivedPhotos(userId).add(photoId.toString());
    }

    private void sendNewPhotoNotification(Long userId, Photo photo) {
		Map<String, Object> photoReceivedFields = new HashMap<>();
        photoReceivedFields.put("TYPE", "OPEN_RECEIVED_PHOTO");
        photoReceivedFields.put("REFERENCE_ID", photo.getId());
        notificationProvider.sendPlainTextNotification(CityUtils.getTrimmedLocationName(photo.getCity()), photoReceivedFields, userId);
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

    private void mapPhotoDetailsDto(Photo photo, PhotoDetailsDto photoDetailsDto, Long userId) {
    	photoDtoMapper.mapToPhotoDto(photo, photoDetailsDto, MAX_LOCATION_NAME_LENGTH_PHOTO_DETAILS);

        photoDetailsDto.setComments(photoCommentService.findFirstByPhotoId(photo.getId(), userId, 3));
    }

    private List<ReceivedPhotoDto> mapToReceivedPhotoDto(List<PhotoReceiver> photos, int maxLocationNameLength) {
        return photos.stream().map(photo -> mapToReceiverPhotoDto(photo, maxLocationNameLength)).collect(Collectors.toList());
    }

    private ReceivedPhotoDto mapToReceiverPhotoDto(PhotoReceiver photo, int maxLocationNameLength) {
        ReceivedPhotoDto photoDto = new ReceivedPhotoDto();
        photoDtoMapper.mapToPhotoDto(photo.getPhoto(), photoDto, maxLocationNameLength);
        photoDto.setOpened(photo.getOpenedDate() != null);

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

    private List<PhotoTraveledDto> mapToPhotoTraveledDto(List<PhotoReceiver> receivedList) {
        return receivedList.stream().map(photo -> mapToPhotoTraveledDto(photo)).collect(Collectors.toList());
    }

    private PhotoTraveledDto mapToPhotoTraveledDto(PhotoReceiver photoReceiver) {
        PhotoTraveledDto dto = new PhotoTraveledDto();
        Long userId = photoReceiver.getUser().getId();
        Long photoId = photoReceiver.getPhoto().getId();
        PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
        if (vote != null) {
        	dto.setLiked(vote.getLike());
        }
        dto.setLocation(CityUtils.getTrimmedLocationName(photoReceiver.getCity()));
        dto.setTimeAgo(prettyTime.format(photoReceiver.getOpenedDate().toDate()));
        dto.setId(photoReceiver.getId());

        return dto;
    }

    private BoundSetOperations<String, String> usersReceivedPhotos(long userId) {
        return redisTemplate.boundSetOps(KeyUtils.userReceivedPhotos(userId));
    }

}
