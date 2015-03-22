package com.sendish.api.service.impl;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.repository.model.jpa.User;

@Service
@Transactional
public class StatisticsServiceImpl {

	@Autowired
	private RedisStatisticsRepository statisticsRepository;
	
	public Long likePhoto(Long photoId, User photoOwner) {
		statisticsRepository.increaseTotalUserLikeCount(photoOwner.getId());	
		
		return statisticsRepository.likePhoto(photoId);
	}

	public Long dislikePhoto(Long photoId, User photoOwner) {
		statisticsRepository.increaseTotalUserDislikeCount(photoOwner.getId());	
		
		return statisticsRepository.dislikePhoto(photoId);
		
	}

	public Long reportPhoto(Long photoId, User photoOwner) {
		statisticsRepository.increaseTotalUserReportCount(photoOwner.getId());	
		
		return statisticsRepository.reportPhoto(photoId);
	}

	public void resetUserUnseenCount(Long userId) {
		statisticsRepository.resetUserUnseenCount(userId);
	}

	public void trackReceivedPhotoOpened(Long photoId, Long userId, Long cityId) {
		statisticsRepository.trackReceivedPhotoOpened(photoId, userId, cityId);
	}

	public void incrementUserUnseenPhotoCount(Long userId) {
		statisticsRepository.incrementUserUnseenPhotoCount(userId);
	}

	public void increaseUserDailyReceivedPhotoCount(Long userId, LocalDate now) {
		statisticsRepository.increaseUserDailyReceivedPhotoCount(userId, now);
	}

}
