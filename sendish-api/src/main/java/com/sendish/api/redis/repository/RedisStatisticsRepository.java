package com.sendish.api.redis.repository;

import com.sendish.api.redis.KeyUtils;
import com.sendish.api.redis.dto.PhotoStatDto;
import org.apache.commons.lang3.ObjectUtils;
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

    // TODO: At some point of time also save stats to DB. Example when one hour elapsed from last update...

    public void likePhoto(Long photoId, Long userId) {
        photoStatistics(photoId).increment("likeCounter", 1);
        userStatistics(userId).increment("likeCounter", 1);
    }

    public void dislikePhoto(Long photoId, Long userId) {
        photoStatistics(photoId).increment("dislikeCounter", 1);
        userStatistics(userId).increment("dislikeCounter", 1);
    }

    public void reportPhoto(Long photoId, Long userId) {
        photoStatistics(photoId).increment("reportCounter", 1);
        userStatistics(userId).increment("reportCounter", 1);
    }

    public PhotoStatDto getPhotoStatistics(Long photoId) {
        List<String> fields = Arrays.asList("likeCounter", "dislikeCounter", "cityCounter", "commentCounter");
        HashOperations<String, String, Long> hashOp = template.opsForHash();
        List<Long> values = hashOp.multiGet(KeyUtils.photoStatistics(photoId), fields);

        Long likeCounter = ObjectUtils.defaultIfNull(values.get(0), 0L);
        Long dislikeCounter = ObjectUtils.defaultIfNull(values.get(1), 0L);
        Long cityCounter = ObjectUtils.defaultIfNull(values.get(2), 0L);
        Long commentCounter = ObjectUtils.defaultIfNull(values.get(3), 0L);

        return new PhotoStatDto(likeCounter, dislikeCounter, cityCounter, commentCounter);
    }

    private RedisMap<String, String> photoStatistics(Long photoId) {
        return new DefaultRedisMap<>(KeyUtils.photoStatistics(photoId), template);
    }

    private RedisMap<String, String> userStatistics(Long userId) {
        return new DefaultRedisMap<>(KeyUtils.userStatistics(userId), template);
    }

}
