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
        HashOperations<String, String, Long> hashOp = template.opsForHash();
        List<Long> values = hashOp.multiGet(KeyUtils.photoStatistics(photoId), fields);

        Long likeCount = ObjectUtils.defaultIfNull(values.get(0), 0L);
        Long dislikeCount = ObjectUtils.defaultIfNull(values.get(1), 0L);
        Long cityCount = ObjectUtils.defaultIfNull(values.get(2), 0L);
        Long commentCount = ObjectUtils.defaultIfNull(values.get(3), 0L);

        return new PhotoStatisticsDto(likeCount, dislikeCount, cityCount, commentCount);
    }

    public UserStatisticsDto getUserStatistics(Long userId) {
        List<String> fields = Arrays.asList("total.likeCount", "total.dislikeCount", "total.reportCount", "daily.sentCount");
        HashOperations<String, String, Long> hashOp = template.opsForHash();
        List<Long> values = hashOp.multiGet(KeyUtils.userStatistics(userId), fields);

        Long likeCount = ObjectUtils.defaultIfNull(values.get(0), 0L);
        Long dislikeCount = ObjectUtils.defaultIfNull(values.get(1), 0L);
        Long reportCount = ObjectUtils.defaultIfNull(values.get(2), 0L);
        Long dailySentCount = ObjectUtils.defaultIfNull(values.get(3), 0L);

        return new UserStatisticsDto(likeCount, dislikeCount, reportCount, dailySentCount);
    }

    public Long increaseDailySentPhotoCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
            userStatistics(userId).put("daily.sentCount", "1");
            userStatistics(userId).put("daily.currentDate", currentDate);
            return 1L;
        } else {
            return userStatistics(userId).increment("daily.sentCount", 1);
        }
    }

    public Integer getDailySentCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
            return 0;
        }

        return Integer.valueOf(userStatistics(userId).get("daily.sentCount"));
    }

    public void increaseDailyReceivedPhotoCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
            userStatistics(userId).put("daily.receivedCount", "1");
            userStatistics(userId).put("daily.currentDate", currentDate);
        } else {
            userStatistics(userId).increment("daily.sentCount", 1);
        }
    }

    public Integer getDailyReceivedCount(Long userId, LocalDate date) {
        String currentDate = userStatistics(userId).get("daily.currentDate");
        if (currentDate == null || !currentDate.equals(date.toString())) {
            return 0;
        }

        return Integer.valueOf(userStatistics(userId).get("daily.receivedCount"));
    }

    private RedisMap<String, String> photoStatistics(Long photoId) {
        return new DefaultRedisMap<>(KeyUtils.photoStatistics(photoId), template);
    }

    private RedisMap<String, String> userStatistics(Long userId) {
        return new DefaultRedisMap<>(KeyUtils.userStatistics(userId), template);
    }

}
