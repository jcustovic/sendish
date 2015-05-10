package com.sendish.api.service.impl;

import com.sendish.api.statistics.DBStatisticsSynchronizer;

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

	@Autowired
	private DBStatisticsSynchronizer dbStatisticsSynchronizer;

	public Long likePhoto(Long photoId, User photoOwner) {
		statisticsRepository.incrementTotalUserLikeCount(photoOwner.getId());
		Long likeCount = statisticsRepository.likePhoto(photoId);

		dbStatisticsSynchronizer.syncUserStat(photoOwner.getId());
		dbStatisticsSynchronizer.syncPhotoStat(photoId);

		return likeCount;
	}

	public Long dislikePhoto(Long photoId, User photoOwner) {
		statisticsRepository.incrementTotalUserDislikeCount(photoOwner.getId());
		Long dislikeCount = statisticsRepository.dislikePhoto(photoId);

		dbStatisticsSynchronizer.syncUserStat(photoOwner.getId());
		dbStatisticsSynchronizer.syncPhotoStat(photoId);

		return dislikeCount;
	}

	public Long reportPhoto(Long photoId, User photoOwner) {
		statisticsRepository.incrementTotalUserReportCount(photoOwner.getId());
		Long reportCount = statisticsRepository.reportPhoto(photoId);

		dbStatisticsSynchronizer.syncUserStat(photoOwner.getId());
		dbStatisticsSynchronizer.syncPhotoStat(photoId);

		return reportCount;
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
	
	public void incrementPhotoReplyWihtPhotoCount(Long photoId) {
		statisticsRepository.incrementPhotoReplyWihtPhotoCount(photoId);
	}
	
	public void decrementPhotoReplyWihtPhotoCount(Long photoId) {
		statisticsRepository.decrementPhotoReplyWihtPhotoCount(photoId);
	}

	public void likeComment(Long photoCommentId) {
		statisticsRepository.likeComment(photoCommentId);
		dbStatisticsSynchronizer.syncPhotoCommentStat(photoCommentId);
	}

	public void dislikeComment(Long photoCommentId) {
		statisticsRepository.dislikeComment(photoCommentId);
		dbStatisticsSynchronizer.syncPhotoCommentStat(photoCommentId);
	}

	public void markActivitiesAsRead(Long userId) {
		statisticsRepository.markActivitiesAsRead(userId);
	}

	public void setNewActivityFlag(Long userId) {
		statisticsRepository.setNewActivityFlag(userId);
	}
	
	public void setNewPhotoReplyActivity(Long userId) {
    	statisticsRepository.setNewPhotoReplyActivity(userId);
    }
    
    public void markPhotoReplyActivitiesAsRead(Long userId) {
    	statisticsRepository.markPhotoReplyActivitiesAsRead(userId);
    }

	public void incrementUnreadInboxItemCount(Long userId) {
		statisticsRepository.incrementUnreadInboxItemCount(userId);
	}

	public void decrementUnreadInboxItemCount(Long userId) {
		statisticsRepository.decrementUnreadInboxItemCount(userId);
	}

}
