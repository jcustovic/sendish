package com.sendish.api.service.impl;

import java.util.List;
import java.util.UUID;

import com.sendish.repository.*;
import com.sendish.repository.model.jpa.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.PhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.mapper.PhotoDtoMapper;

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
    private ImageRepository imageRepository;

    @Autowired
    private UserInboxServiceImpl userInboxService;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private PhotoRepository photoRepository;

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

	public PhotoDetailsDto findByPhotoIdForUser(Long photoId, Long userId) {
		HotPhoto hotPhoto = findByPhotoId(photoId);
		if (hotPhoto == null) {
			return null;
		}

		return photoDtoMapper.mapToPhotoDetailsDto(hotPhoto.getPhoto(), userId, MAX_LOCATION_NAME_LENGTH_PHOTO_DETAILS);
	}

    public void newHotPhoto(Long photoId) {
        HotPhoto hotPhoto = new HotPhoto();
        hotPhoto.setPhotoId(photoId);
        hotPhoto.setSelectedTime(DateTime.now());

        hotPhotoRepository.save(hotPhoto);

        // photoSenderService.stopSending(photoId, "Photo reached hot list");

        Photo photo = photoRepository.findOne(photoId);
        sendCongratsInboxItem(photo);
    }

    private void sendCongratsInboxItem(Photo photo) {
        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setShortTitle("Congrats! Your photo just ended up on hot list.");
        inboxMessage.setTitle("Congratulations!");
        inboxMessage.setMessage("Your photo made the hot list. Keep sendishing :)");
        inboxMessage.setImage(createImageOutOfPhoto(photo));

        inboxMessage = inboxMessageRepository.save(inboxMessage);
        userInboxService.sendInboxMessage(inboxMessage.getId(), photo.getUser().getId());
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
