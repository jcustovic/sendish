package com.sendish.api.service.impl;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.redis.dto.CommentStatisticsDto;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.dto.UserStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.repository.model.jpa.User;

@Service
@Transactional
public class StatisticsServiceImpl {

	@Autowired
	private RedisStatisticsRepository statisticsRepository;

    // TODO: At some point of time also save stats to DB (User, Photo, Comment). Example when one hour elapsed from last update
    // or photo stops traveling etc. ... UserStatisticsRepository.java, PhotoStatisticsRepository.java
	
	public Long likePhoto(Long photoId, User photoOwner) {
		statisticsRepository.incrementTotalUserLikeCount(photoOwner.getId());	
		
		return statisticsRepository.likePhoto(photoId);
	}

	public Long dislikePhoto(Long photoId, User photoOwner) {
		statisticsRepository.incrementTotalUserDislikeCount(photoOwner.getId());	
		
		return statisticsRepository.dislikePhoto(photoId);
		
	}

	public Long reportPhoto(Long photoId, User photoOwner) {
		statisticsRepository.incrementTotalUserReportCount(photoOwner.getId());	
		
		return statisticsRepository.reportPhoto(photoId);
	}

	public void resetUserUnseenCount(Long userId) {
		statisticsRepository.resetUserUnseenCount(userId);
	}

	public void resetUnreadInboxItemCount(Long userId) {
		statisticsRepository.resetUnreadInboxItemCount(userId);
	}

	public void trackReceivedPhotoOpened(Long photoId, Long userId, Long cityId) {
		statisticsRepository.trackReceivedPhotoOpened(photoId, userId, cityId);
	}

	public void incrementUserUnseenPhotoCount(Long userId) {
		statisticsRepository.incrementUserUnseenPhotoCount(userId);
	}

	public Long increaseUserDailyReceivedPhotoCount(Long userId, LocalDate now) {
		return statisticsRepository.incrementUserDailyReceivedPhotoCount(userId, now);
	}

	public PhotoStatisticsDto getPhotoStatistics(Long photoId) {
		return statisticsRepository.getPhotoStatistics(photoId);
	}

	public UserStatisticsDto getUserStatistics(Long userId) {
		return statisticsRepository.getUserStatistics(userId);
	}
	
	public CommentStatisticsDto getCommentStatistics(Long photoCommentId) {
		return statisticsRepository.getCommentStatistics(photoCommentId);
	}

	public Long incrementUserDailySentPhotoCount(Long userId, LocalDate now) {
		return statisticsRepository.incrementUserDailySentPhotoCount(userId, now);
	}

	public void incrementPhotoCommentCount(Long photoId) {
		statisticsRepository.incrementPhotoCommentCount(photoId);
	}
	
	public void decrementPhotoCommentCount(Long photoId) {
		statisticsRepository.decrementPhotoCommentCount(photoId);
	}

	public void likeComment(Long photoCommentId) {
		statisticsRepository.likeComment(photoCommentId);
	}

	public void dislikeComment(Long photoCommentId) {
		statisticsRepository.dislikeComment(photoCommentId);
	}

	public void markActivitiesAsRead(Long userId) {
		statisticsRepository.markActivitiesAsRead(userId);
	}

	public void setNewActivityFlag(Long userId) {
		statisticsRepository.setNewActivityFlag(userId);
	}

	public void incrementUnreadInboxItemCount(Long userId) {
		statisticsRepository.incrementUnreadInboxItemCount(userId);
	}

	public void decrementUnreadInboxItemCount(Long userId) {
		statisticsRepository.decrementUnreadInboxItemCount(userId);
	}

}
