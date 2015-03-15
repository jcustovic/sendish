package com.sendish.api.service.impl;

import java.util.List;

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
	
	@Autowired
	private HotPhotoRepository hotPhotoRepository;
	
	@Autowired
	private PhotoDtoMapper photoDtoMapper;
	
	@Autowired
	private PhotoCommentServiceImpl photoCommentService;
	
	@Autowired
    private PhotoVoteRepository photoVoteRepository;

	public List<PhotoDto> findAllActive(Integer page) {
		List<HotPhoto> photos = hotPhotoRepository.findAllActive(new PageRequest(page, HOT_PHOTO_PAGE_SIZE, Direction.DESC, "selectedTime"));
		
		return photoDtoMapper.mapHotToPhotoDto(photos);
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
		photoDtoMapper.mapToPhotoDto(hotPhoto.getPhoto(), photoDetails);
		photoDetails.setComments(photoCommentService.findFirstByPhotoId(photoId, userId, 3));
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
        if (vote != null) {
        	photoDetails.setLike(vote.getLike());
        }
		
		return photoDetails;
	}

}
