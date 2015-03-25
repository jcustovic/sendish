package com.sendish.api.service.impl;

import java.util.List;
import java.util.UUID;

import com.sendish.repository.ImageRepository;
import com.sendish.repository.InboxMessageRepository;
import com.sendish.repository.model.jpa.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.HotPhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.mapper.PhotoDtoMapper;
import com.sendish.repository.HotPhotoRepository;
import com.sendish.repository.PhotoVoteRepository;

@Service
@Transactional
public class HotPhotoServiceImpl {
	
	private static final int HOT_PHOTO_PAGE_SIZE = 20;
    private static final int MAX_LOCATION_NAME_LENGTH_PHOTO_DETAILS = 24;
    private static final int MAX_LOCATION_NAME_LENGTH_PHOTO_LIST = 30;
	
	@Autowired
	private HotPhotoRepository hotPhotoRepository;
	
	@Autowired
	private PhotoDtoMapper photoDtoMapper;
	
	@Autowired
	private PhotoCommentServiceImpl photoCommentService;
	
	@Autowired
    private PhotoVoteRepository photoVoteRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserInboxServiceImpl userInboxService;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    public List<PhotoDto> findAll(Integer page) {
        Page<HotPhoto> photos = hotPhotoRepository.findAll(new PageRequest(page, HOT_PHOTO_PAGE_SIZE, Direction.DESC, "selectedTime"));

        return photoDtoMapper.mapHotToPhotoDto(photos.getContent(), MAX_LOCATION_NAME_LENGTH_PHOTO_LIST);
    }

	public List<PhotoDto> findAllActive(Integer page) {
		List<HotPhoto> photos = hotPhotoRepository.findAllActive(new PageRequest(page, HOT_PHOTO_PAGE_SIZE, Direction.DESC, "selectedTime"));
		
		return photoDtoMapper.mapHotToPhotoDto(photos, MAX_LOCATION_NAME_LENGTH_PHOTO_LIST);
	}

	public HotPhoto findPhotoByPhotoUuid(String photoUUID) {
		return hotPhotoRepository.findByPhotoUuid(photoUUID);
	}

	public HotPhoto findByPhotoId(Long photoId) {
		return hotPhotoRepository.findOne(photoId);
	}

	public HotPhotoDetailsDto findByPhotoIdForUser(Long photoId, Long userId) {
		HotPhoto hotPhoto = findByPhotoId(photoId);
		if (hotPhoto == null) {
			return null;
		}
		HotPhotoDetailsDto photoDetails = new HotPhotoDetailsDto();
		photoDtoMapper.mapToPhotoDto(hotPhoto.getPhoto(), photoDetails, MAX_LOCATION_NAME_LENGTH_PHOTO_DETAILS);
		photoDetails.setComments(photoCommentService.findFirstByPhotoId(photoId, userId, 3));
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
        if (vote != null) {
        	photoDetails.setLike(vote.getLike());
        }
		
		return photoDetails;
	}

    public void addNewPhoto(Long photoId) {
        HotPhoto hotPhoto = new HotPhoto();
        hotPhoto.setPhotoId(photoId);
        hotPhoto.setSelectedTime(DateTime.now());

        hotPhoto = hotPhotoRepository.save(hotPhoto);
        sendCongratsInboxItem(hotPhoto);
    }

    private void sendCongratsInboxItem(HotPhoto hotPhoto) {
        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setShortTitle("Your photo ended on hot list!");
        inboxMessage.setTitle("Congrats! Your photo is on hot list");
        inboxMessage.setMessage("Woho! Know everybody can see your photo!");
        inboxMessage.setImage(createImageOutOfPhoto(hotPhoto.getPhoto()));

        inboxMessage = inboxMessageRepository.save(inboxMessage);
        userInboxService.sendInboxMessage(inboxMessage.getId(), hotPhoto.getPhoto().getUser().getId());
    }

    private Image createImageOutOfPhoto(Photo photo) {
        Image image = new Image();
        image.setContentType(photo.getContentType());
        image.setHeight(photo.getHeight());
        image.setWidth(photo.getWidth());
        image.setSize(photo.getSize());
        image.setUuid(UUID.randomUUID().toString());
        image.setStorageId(photo.getStorageId());
        image.setName(photo.getName());

        return imageRepository.save(image);
    }

    public void remove(Long photoId) {
        HotPhoto hotPhoto = hotPhotoRepository.findOne(photoId);
        hotPhoto.setRemovedTime(DateTime.now());

        hotPhotoRepository.save(hotPhoto);
    }

}
