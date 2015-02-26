package com.sendish.api.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.sendish.api.dto.PhotoDto;
import com.sendish.api.mapper.PhotoDtoMapper;
import com.sendish.repository.HotPhotoRepository;
import com.sendish.repository.model.jpa.HotPhoto;

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

}
