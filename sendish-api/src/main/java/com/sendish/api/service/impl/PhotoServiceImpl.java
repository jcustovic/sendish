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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.sendish.api.photo.HotPhotoDecider;
import com.sendish.api.photo.PhotoStopDecider;
import com.sendish.api.redis.KeyUtils;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.api.store.FileStore;
import com.sendish.api.util.CityUtils;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.PhotoStatisticsRepository;
import com.sendish.repository.PhotoVoteRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;
import com.sendish.repository.model.jpa.PhotoSendingDetails;
import com.sendish.repository.model.jpa.PhotoStatistics;
import com.sendish.repository.model.jpa.PhotoVote;
import com.sendish.repository.model.jpa.PhotoVoteId;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserDetails;

@Service
@Transactional
public class PhotoServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PhotoServiceImpl.class);

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
    private PhotoStatisticsRepository photoStatisticsRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PhotoCommentServiceImpl photoCommentService;

    @Autowired
    private RedisStatisticsRepository statisticsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AsyncPhotoSenderServiceImpl asyncPhotoSenderService;
    
    @Autowired
    private PhotoSenderServiceImpl photoSenderService;
    
    @Autowired
    private AsyncNotificationProvider notificationProvider;
    
    @Autowired
    private PhotoDtoMapper photoDtoMapper;
    
    @Autowired
    private RankingServiceImpl rankingService;
    
    @Autowired
    private UserActivityServiceImpl userActivityService;
    
    @Autowired
    private PhotoVoteRepository photoVoteRepository;
    
    @Autowired
    private PhotoStopDecider photoStopDecider;
    
    @Autowired
    private HotPhotoDecider hotPhotoDecider;

    public Photo findOne(Long photoId) {
        return photoRepository.findOne(photoId);
    }

    public PhotoSendingDetails processNewImage(LocationBasedFileUpload upload, Long userId) {
        DateTime receivedDate = DateTime.now();
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
            fileStoreId = fileStore.save(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        photo.setStorageId(fileStoreId);

        Location location = new Location(upload.getLatitude(), upload.getLongitude());
        City city = cityService.findNearest(upload.getLatitude(), upload.getLongitude());
        photo.setOriginLocation(location);
        photo.setCity(city);

        photo = photoRepository.save(photo);
        createPhotoStatistics(photo);
        userService.updateStatisticsForNewSentPhoto(userId, receivedDate, location, city);
        rankingService.addPointsForNewSendish(userId);
        
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

    public List<ReceivedPhotoDto> findReceivedByUserId(Long userId, Integer page) {
        List<PhotoReceiver> photos = photoReceiverRepository.findByUserId(userId, 
        		new PageRequest(page, PHOTO_PAGE_SIZE, Direction.DESC, "createdDate"));
        
        if (page == 0) {
			statisticsRepository.resetUnseenCount(userId);
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

    public ReceivedPhotoDetailsDto openReceivedByPhotoIdAndUserId(Long photoId, Long userId, BigDecimal longitude, BigDecimal latitude) {
        PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
        if (photoReceiver == null) {
            return null;
        }

        ReceivedPhotoDetailsDto photoDetailsDto = new ReceivedPhotoDetailsDto();
        if (photoReceiver.getOpenedDate() == null) {
            photoReceiver.setOpenedDate(DateTime.now());
            Location location = getUserLocation(userId, longitude, latitude);
            photoReceiver.setOpenedLocation(location);
            City city = cityService.findNearest(location.getLatitude(), location.getLongitude());
            photoReceiver.setCity(city);
            photoReceiverRepository.save(photoReceiver);
            statisticsRepository.trackReceivedPhotoOpened(photoId, userId, city.getId());
        } else {
        	PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
        	if (vote != null) {
        		photoDetailsDto.setLike(vote.getLike());
                photoDetailsDto.setReport(vote.getReport());		
        	} else {
        		// TODO: Remove this. I use it just to see how often this happens
        		LOGGER.error("Photo with id {} was opened by user with id {} but vote not found", photoId, userId);
        	}
        }
        
        mapPhotoDetailsDto(photoReceiver.getPhoto(), photoDetailsDto, userId);

        return photoDetailsDto;
    }

    public void likeReceived(Long photoId, Long userId) {
    	PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
    	if (vote == null) {
    		savePhotoLike(photoId, userId);
    		PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
    		asyncPhotoSenderService.resendPhotoOnLike(photoId, photoReceiver.getId());
    	}
    }

    // TODO: Maybe allow changing dislike to like?
    public void likePhoto(Long photoId, Long userId) {
    	PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
    	if (vote == null) {
    		savePhotoLike(photoId, userId);
    	}
	}

	private void savePhotoLike(Long photoId, Long userId) {
		Photo photo = photoRepository.findOne(photoId);
        Long photoOwnerId = photo.getUser().getId();

        if (photoOwnerId.equals(userId)) {
            throw new IllegalStateException("User cannot like his own photo. UserId: " + userId + ", PhotoId: " + photoId);
        }

		User user = userRepository.findOne(userId);
		
		PhotoVote vote = new PhotoVote();
		vote.setPhoto(photo);
		vote.setUser(user);
		vote.setLike(true);
		photoVoteRepository.save(vote);

		Long likeCount = statisticsRepository.likePhoto(photoId, photoOwnerId);
		hotPhotoDecider.decide(photoId, likeCount);
		rankingService.addPointsForLikedPhoto(photoOwnerId);

		if (!photo.getDeleted()) {
			userActivityService.addPhotoLikedActivity(photo, user);
		}
	}

    // TODO: Maybe allow changing like to dislike?
    public void dislikeReceived(Long photoId, Long userId) {
    	PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
    	if (vote == null) {
    		savePhotoDislike(photoId, userId);
    		PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
    		photoReceiver.setDeleted(true);
            photoReceiverRepository.save(photoReceiver);
            
            if (photoStopDecider.checkToStop(photoId)) {
            	photoSenderService.stopSending(photoId, "Stopped after dislike check");
            }
    	}
    }

	public void dislikePhoto(Long photoId, Long userId) {
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
    	if (vote == null) {
    		savePhotoDislike(photoId, userId);
    	}
	}

	private void savePhotoDislike(Long photoId, Long userId) {
		Photo photo = photoRepository.findOne(photoId);
        Long photoOwnerId = photo.getUser().getId();

        if (photoOwnerId.equals(userId)) {
            throw new IllegalStateException("User cannot dislike his own photo. UserId: " + userId + ", PhotoId: " + photoId);
        }

		User user = userRepository.findOne(userId);
		
		PhotoVote vote = new PhotoVote();
		vote.setPhoto(photo);
		vote.setUser(user);
		vote.setLike(false);
		photoVoteRepository.save(vote);

		rankingService.removePointsForDislikedPhoto(photoOwnerId);
		statisticsRepository.dislikePhoto(photoId, photoOwnerId);
	}

    public void reportReceived(Long photoId, String reason, String reasonText, Long userId) {
    	PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
    	if (vote == null) {
    		PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
            Long photoOwnerId = photoReceiver.getPhoto().getUser().getId();

            if (photoOwnerId.equals(userId)) {
                throw new IllegalStateException("User cannot report his own photo. UserId: " + userId + ", PhotoId: " + photoId);
            }
    		
    		vote = new PhotoVote();
    		vote.setPhoto(photoReceiver.getPhoto());
    		vote.setUser(photoReceiver.getUser());
    		vote.setLike(false);
    		vote.setReport(true);
    		vote.setReportType(reason);
    		vote.setReportText(reasonText);
    		photoVoteRepository.save(vote);
            
            photoReceiver.setDeleted(true);
            photoReceiverRepository.save(photoReceiver);

            statisticsRepository.reportPhoto(photoId, photoOwnerId);
            rankingService.removePointsForReportedPhoto(photoOwnerId);
            
            if (photoStopDecider.checkToStop(photoId)) {
            	photoSenderService.stopSending(photoId, "Stopped after report check");
            }
    	}
    }

    public List<PhotoTraveledDto> getTraveledLocations(Long photoId, Integer page) {
        List<PhotoReceiver> receivedList = photoReceiverRepository.findByPhotoIdAndOpenedDateNotNull(photoId, new PageRequest(page, PHOTO_LOCATION_PAGE_SIZE, Direction.DESC, "createdDate"));

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
        UserDetails userDetails = userService.getUserDetails(userId);
        Photo photo = photoRepository.findOne(photoId);

        PhotoReceiver photoReceiver = new PhotoReceiver();
        photoReceiver.setUser(userDetails.getUser());
        photoReceiver.setPhoto(photo);
        photoReceiverRepository.save(photoReceiver);

        DateTime now = DateTime.now();
        userDetails.setLastReceivedTime(now);
        userService.saveUserDetails(userDetails);

        // TODO: Move to separate method after PhotoReceiver has been saved and committed!
        usersReceivedPhotos(userId).add(photoId.toString());
        statisticsRepository.incrementUnseenCount(userId);
        statisticsRepository.increaseDailyReceivedPhotoCount(userId, now.toLocalDate());
        
        if (userDetails.getReceiveNewPhotoNotifications()) {
        	sendNewPhotoNotification(userId, photo);
        }

        return photoReceiver;
    }
    
    private void sendNewPhotoNotification(Long userId, Photo photo) {
		Map<String, Object> photoReceivedFields = new HashMap<>();
        photoReceivedFields.put("TYPE", "RECEIVED_PHOTO");
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

    private void createPhotoStatistics(Photo photo) {
        PhotoStatistics photoStatistics = new PhotoStatistics();
        photoStatistics.setPhotoId(photo.getId());
        photoStatisticsRepository.save(photoStatistics);
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
        dto.setTimeAgo(prettyTime.format(photoReceiver.getCreatedDate().toDate()));
        dto.setId(photoReceiver.getId());

        return dto;
    }

    public BoundSetOperations<String, String> usersReceivedPhotos(long userId) {
        return redisTemplate.boundSetOps(KeyUtils.userReceivedPhotos(userId));
    }

}
