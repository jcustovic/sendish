package com.sendish.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.PhotoStatisticsRepository;
import com.sendish.repository.model.jpa.PhotoStatistics;

@Service
@Transactional
public class PhotoStatisticsServiceImpl {
	
	@Autowired
	private PhotoStatisticsRepository photoStatisticsRepository;

	public PhotoStatistics createNew(Long photoId) {
        PhotoStatistics photoStatistics = new PhotoStatistics();
        photoStatistics.setPhotoId(photoId);
        
        return photoStatisticsRepository.save(photoStatistics);
    }
	
}
