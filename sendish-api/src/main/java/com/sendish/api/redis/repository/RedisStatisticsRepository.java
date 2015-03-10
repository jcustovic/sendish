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

    public Long likePhoto(Long photoId, Long photoOwnerId) {
        photoStatistics(photoId).increment("likeCount", 1);
        return userStatistics(photoOwnerId).increment("total.likeCount", 1);
    }

    public Long dislikePhoto(Long photoId, Long photoOwnerId) {
        photoStatistics(photoId).increment("dislikeCount", 1);
        return userStatistics(photoOwnerId).increment("total.dislikeCount", 1);
    }

    public void reportPhoto(Long photoId, Long photoOwnerId) {
        photoStatistics(photoId).increment("reportCount", 1);
        userStatistics(photoOwnerId).increment("total.reportCount", 1);
    }

    public void increasePhotoCommentCount(Long photoId) {
        photoStatistics(photoId).increment("commentCount", 1);
    }

    public PhotoStatisticsDto getPhotoStatistics(Long photoId) {
        List<String> fields = Arrays.asList("likeCount", "dislikeCount", "commentCount");
        HashOperations<String, String, String> hashOp = template.opsForHash();
        List<String> values = hashOp.multiGet(KeyUtils.photoStatistics(photoId), fields);

        Long likeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(0), "0"));
        Long dislikeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(1), "0"));
        Long commentCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(2), "0"));
        Long cityCount = photoCities(photoId).size(); 

        return new PhotoStatisticsDto(likeCount, dislikeCount, cityCount, commentCount);
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
        
        long dailySentCount = getCurrentDailySentCount(userId, LocalDate.now());
        long totalCityCount = userCities(userId).size();

		return new UserStatisticsDto(likeCount, dislikeCount, reportCount,
				dailySentCount, unseenPhotoCount, unreadInboxItemCount,
				totalCityCount, hasNewActivities);
    }

    public Long increaseDailySentPhotoCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
        	userStatistics(userId).put("daily.sentCount", "0");
            userStatistics(userId).put("daily.currentDate", date.toString());
        }

        return userStatistics(userId).increment("daily.sentCount", 1);
    }

    public Long getCurrentDailySentCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
            return 0L;
        }

        return Long.valueOf(userStatistics(userId).get("daily.sentCount"));
    }

    public void increaseDailyReceivedPhotoCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
        	userStatistics(userId).put("daily.receivedCount", "0");
            userStatistics(userId).put("daily.currentDate", currentDate);
        }

        userStatistics(userId).increment("daily.receivedCount", 1);
    }

    public Long getDailyReceivedCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
            return 0L;
        }

        return Long.valueOf(userStatistics(userId).get("daily.receivedCount"));
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

    public void incrementUnseenCount(Long userId) {
        userStatistics(userId).increment("total.unseenPhotoCount", 1);
    }

    public void decrementUnseenCount(Long userId) {
        userStatistics(userId).increment("total.unseenPhotoCount", -1);
    }
    
    public void incrementUnreadInboxItemCount(Long userId) {
        userStatistics(userId).increment("total.unreadInboxItemCount", 1);
    }
    
    public void decrementUnreadInboxItemCount(Long userId) {
        userStatistics(userId).increment("total.unreadInboxItemCount", -1);
    }
    
    public void trackCity(Long photoId, Long userId, Long cityId) {
    	String cityString = cityId.toString();
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
