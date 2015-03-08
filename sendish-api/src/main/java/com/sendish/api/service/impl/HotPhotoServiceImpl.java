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
import com.sendish.repository.model.jpa.HotPhoto;
import com.sendish.repository.model.jpa.Photo;

@Service
@Transactional
public class HotPhotoServiceImpl {
	
	private static final int HOT_PHOTO_PAGE_SIZE = 20;
	
	@Autowired
	private HotPhotoRepository hotPhotoRepository;
	
	@Autowired
	private PhotoDtoMapper photoDtoMapper;

	public List<PhotoDto> findAllActive(Integer page) {
		List<HotPhoto> photos = hotPhotoRepository.findAllActive(new PageRequest(page, HOT_PHOTO_PAGE_SIZE, Direction.DESC, "selectedTime"));
		
		return photoDtoMapper.mapHotToPhotoDto(photos);
	}

	public Photo findPhotoByPhotoUuid(String photoUUID) {
		HotPhoto hotPhoto = hotPhotoRepository.findByPhotoUuid(photoUUID);
		
		return hotPhoto.getPhoto();
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
		// TODO: Implement like flag on HotPhotoDetailsDto
		
		return photoDetails;
	}

}
