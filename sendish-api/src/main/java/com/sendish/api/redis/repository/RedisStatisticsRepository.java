package com.sendish.api.redis.repository;

import com.sendish.api.redis.KeyUtils;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.dto.UserStatisticsDto;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class RedisStatisticsRepository {

    @Autowired
    private StringRedisTemplate template;

    // TODO: At some point of time also save stats to DB (User & Photo). Example when one hour elapsed from last update
    // or photo stops traveling etc. ... UserStatisticsRepository.java, PhotoStatisticsRepository.java
    // TODO: Photo & User keep track of all the cities

    public void likePhoto(Long photoId, Long userId) {
        photoStatistics(photoId).increment("likeCount", 1);
        userStatistics(userId).increment("total.likeCount", 1);
    }

    public void dislikePhoto(Long photoId, Long userId) {
        photoStatistics(photoId).increment("dislikeCount", 1);
        userStatistics(userId).increment("total.dislikeCount", 1);
    }

    public void reportPhoto(Long photoId, Long userId) {
        photoStatistics(photoId).increment("reportCount", 1);
        userStatistics(userId).increment("total.reportCount", 1);
    }

    public PhotoStatisticsDto getPhotoStatistics(Long photoId) {
        List<String> fields = Arrays.asList("likeCount", "dislikeCount", "cityCount", "commentCount");
        HashOperations<String, String, String> hashOp = template.opsForHash();
        List<String> values = hashOp.multiGet(KeyUtils.photoStatistics(photoId), fields);

        Long likeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(0), "0"));
        Long dislikeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(1), "0"));
        Long cityCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(2), "0"));
        Long commentCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(3), "0"));

        return new PhotoStatisticsDto(likeCount, dislikeCount, cityCount, commentCount);
    }

    public UserStatisticsDto getUserStatistics(Long userId) {
        List<String> fields = Arrays.asList("total.likeCount", "total.dislikeCount", "total.reportCount", "daily.sentCount");
        HashOperations<String, String, String> hashOp = template.opsForHash();
        List<String> values = hashOp.multiGet(KeyUtils.userStatistics(userId), fields);

        Long likeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(0), "0"));
        Long dislikeCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(1), "0"));
        Long reportCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(2), "0"));
        Long dailySentCount = Long.valueOf(ObjectUtils.defaultIfNull(values.get(3), "0"));

        return new UserStatisticsDto(likeCount, dislikeCount, reportCount, dailySentCount);
    }

    public Long increaseDailySentPhotoCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
        	userStatistics(userId).put("daily.sentCount", "0");
            userStatistics(userId).put("daily.currentDate", date.toString());
        }

        return userStatistics(userId).increment("daily.sentCount", 1);
    }

    public Long getDailySentCount(Long userId, LocalDate date) {
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

    private RedisMap<String, String> photoStatistics(Long photoId) {
        return new DefaultRedisMap<>(KeyUtils.photoStatistics(photoId), template);
    }
    
    private RedisMap<String, String> userStatistics(Long userId) {
        return new DefaultRedisMap<>(KeyUtils.userStatistics(userId), template);
    }

}
