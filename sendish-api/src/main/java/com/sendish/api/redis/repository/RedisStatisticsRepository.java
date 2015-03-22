package com.sendish.api.redis.repository;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Repository;

import com.sendish.api.redis.KeyUtils;
import com.sendish.api.redis.dto.CommentStatisticsDto;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.dto.UserStatisticsDto;

@Repository
public class RedisStatisticsRepository {

    @Autowired
    private StringRedisTemplate template;

    // TODO: At some point of time also save stats to DB (User & Photo). Example when one hour elapsed from last update
    // or photo stops traveling etc. ... UserStatisticsRepository.java, PhotoStatisticsRepository.java

    public Long likePhoto(Long photoId) {
        return photoStatistics(photoId).increment("likeCount", 1);
    }

    public Long dislikePhoto(Long photoId) {
        return photoStatistics(photoId).increment("dislikeCount", 1);
    }

    public Long reportPhoto(Long photoId) {
        return photoStatistics(photoId).increment("reportCount", 1);
    }
    
    public Long increaseTotalUserLikeCount(Long userId) {
    	return userStatistics(userId).increment("total.likeCount", 1);    	
    }
    
    public Long increaseTotalUserDislikeCount(Long userId) {
    	return userStatistics(userId).increment("total.dislikeCount", 1);    	
    }
    
    public Long increaseTotalUserReportCount(Long userId) {
    	return userStatistics(userId).increment("total.reportCount", 1);    	
    }

    public Long increasePhotoCommentCount(Long photoId) {
        return photoStatistics(photoId).increment("commentCount", 1);
    }

    public PhotoStatisticsDto getPhotoStatistics(Long photoId) {
        List<String> fields = Arrays.asList("openedCount", "likeCount", "dislikeCount", "commentCount", "reportCount");
        HashOperations<String, String, String> hashOp = template.opsForHash();
        List<String> values = hashOp.multiGet(KeyUtils.photoStatistics(photoId), fields);

        Long openedCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(0), "0"));
        Long likeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(1), "0"));
        Long dislikeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(2), "0"));
        Long commentCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(3), "0"));
        Long reportCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(4), "0"));
        Long cityCount = photoCities(photoId).size(); 

        return new PhotoStatisticsDto(openedCount, likeCount, dislikeCount, cityCount, commentCount, reportCount);
    }

    public UserStatisticsDto getUserStatistics(Long userId) {
		List<String> fields = Arrays.asList("total.likeCount",
				"total.dislikeCount", "total.reportCount",
				"total.unseenPhotoCount", "total.unreadInboxItemCount",
				"hasNewActivities");
        HashOperations<String, String, String> hashOp = template.opsForHash();
        List<String> values = hashOp.multiGet(KeyUtils.userStatistics(userId), fields);

        long likeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(0), "0"));
        long dislikeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(1), "0"));
        long reportCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(2), "0"));
        long unseenPhotoCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(3), "0"));
        long unreadInboxItemCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(4), "0"));
        boolean hasNewActivities = values.get(5) != null;
        
        long dailySentCount = getDailySentPhotoCount(userId, LocalDate.now());
        long totalCityCount = userCities(userId).size();

		return new UserStatisticsDto(likeCount, dislikeCount, reportCount,
				dailySentCount, unseenPhotoCount, unreadInboxItemCount,
				totalCityCount, hasNewActivities);
    }

    public Long increaseDailySentPhotoCount(Long userId, LocalDate date) {
        checkIfNewDayAndReset(userId, date);

        return userStatistics(userId).increment("daily.sentCount", 1);
    }
    
    public Long getDailySentPhotoCount(Long userId, LocalDate date) {
        if (checkIfNewDayAndReset(userId, date)){ 
            return 0L;
        }
        
        String sentCountString = userStatistics(userId).get("daily.sentCount");

        return Long.valueOf(ObjectUtils.defaultIfNull(sentCountString, "0"));
    }

    public void increaseUserDailyReceivedPhotoCount(Long userId, LocalDate date) {
    	checkIfNewDayAndReset(userId, date);

        userStatistics(userId).increment("daily.receivedCount", 1);
    }

	public Long getDailyReceivedPhotoCount(Long userId, LocalDate date) {
		if (checkIfNewDayAndReset(userId, date)){ 
            return 0L;
        }
        
        String receivedCountString = userStatistics(userId).get("daily.receivedCount");

        return Long.valueOf(ObjectUtils.defaultIfNull(receivedCountString, "0"));
    }
	
	private boolean checkIfNewDayAndReset(Long userId, LocalDate date) {
    	String currentDate = userStatistics(userId).get("daily.currentDate");
        String stringDate = date.toString();
        if (currentDate == null || !currentDate.equals(stringDate)) {
        	userStatistics(userId).put("daily.receivedCount", "0");
    		userStatistics(userId).put("daily.sentCount", "0");
            userStatistics(userId).put("daily.currentDate", stringDate);
        	return true;
        }
        
        return false;
    }

    public void likeComment(Long photoCommentId) {
        photoCommentStatistics(photoCommentId).increment("likeCount", 1);
    }

    public void dislikeComment(Long photoCommentId) {
        photoCommentStatistics(photoCommentId).increment("dislikeCount", 1);
    }

    public CommentStatisticsDto getCommentStatistics(Long photoCommentId) {
        List<String> fields = Arrays.asList("likeCount", "dislikeCount");
        HashOperations<String, String, String> hashOp = template.opsForHash();
        List<String> values = hashOp.multiGet(KeyUtils.photoCommentStatistics(photoCommentId), fields);

        Long likeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(0), "0"));
        Long dislikeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(1), "0"));

        return new CommentStatisticsDto(likeCount, dislikeCount);
    }

    public void incrementUserUnseenPhotoCount(Long userId) {
        userStatistics(userId).increment("total.unseenPhotoCount", 1);
    }
    
    public void resetUserUnseenCount(Long userId) {
        userStatistics(userId).put("total.unseenPhotoCount", "0");
    }
    
    public void incrementUnreadInboxItemCount(Long userId) {
        userStatistics(userId).increment("total.unreadInboxItemCount", 1);
    }
    
    public void decrementUnreadInboxItemCount(Long userId) {
        userStatistics(userId).increment("total.unreadInboxItemCount", -1);
    }
    
    public void trackReceivedPhotoOpened(Long photoId, Long userId, Long cityId) {
    	String cityString = cityId.toString();
    	photoStatistics(photoId).increment("openedCount", 1);
    	photoCities(photoId).add(cityString);
    	userCities(userId).add(cityString);
	}
    
    public void newActivity(Long userId) {
    	userStatistics(userId).putIfAbsent("hasNewActivities", "1");
    }
    
    public void markActivitiesAsRead(Long userId) {
    	userStatistics(userId).remove("hasNewActivities");
    }
    
    // Redis objects

    private RedisMap<String, String> photoStatistics(Long photoId) {
        return new DefaultRedisMap<>(KeyUtils.photoStatistics(photoId), template);
    }
    
    private RedisMap<String, String> userStatistics(Long userId) {
        return new DefaultRedisMap<>(KeyUtils.userStatistics(userId), template);
    }
    
    private RedisMap<String, String> photoCommentStatistics(Long photoCommentId) {
        return new DefaultRedisMap<>(KeyUtils.photoCommentStatistics(photoCommentId), template);
    }

    private BoundSetOperations<String, String> photoCities(Long photoId) {
    	return template.boundSetOps(KeyUtils.photoCities(photoId));
    }
    
    private BoundSetOperations<String, String> userCities(Long userId) {
    	return template.boundSetOps(KeyUtils.userCities(userId));
    }

}
