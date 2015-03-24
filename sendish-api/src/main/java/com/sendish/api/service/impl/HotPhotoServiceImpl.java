package com.sendish.api.service.impl;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.HotPhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.mapper.PhotoDtoMapper;
import com.sendish.repository.HotPhotoRepository;
import com.sendish.repository.PhotoVoteRepository;
import com.sendish.repository.model.jpa.HotPhoto;
import com.sendish.repository.model.jpa.PhotoVote;
import com.sendish.repository.model.jpa.PhotoVoteId;

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
    private UserInboxServiceImpl userInboxService;

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
        // TODO: Create new InboxMessage and send it to the photo owner.
    }

    public void remove(Long photoId) {
        HotPhoto hotPhoto = hotPhotoRepository.findOne(photoId);
        hotPhoto.setRemovedTime(DateTime.now());

        hotPhotoRepository.save(hotPhoto);
    }

}
