package com.sendish.api.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisDBStatisticsSynchronizer implements DBStatisticsSynchronizer {

    private static final String USERS_TO_UPDATE_SET = "statistics:sync:db:users";
    private static final String PHOTOS_TO_UPDATE_SET = "statistics:sync:db:photos";
    private static final String PHOTO_COMMENTS_TO_UPDATE_SET = "statistics:sync:db:photoComments";

    @Autowired
    private StringRedisTemplate template;

    @Override
    public void syncUserStat(Long userId) {
        usersToUpdate().add(userId.toString());
    }

    @Override
    public void syncPhotoStat(Long photoId) {
        photosToUpdate().add(photoId.toString());
    }

    @Override
    public void syncPhotoCommentStat(Long photoCommentId) {
        photoCommentsToUpdate().add(photoCommentId.toString());
    }

    public BoundSetOperations<String, String> usersToUpdate() {
        return template.boundSetOps(USERS_TO_UPDATE_SET);
    }

    public BoundSetOperations<String, String> photosToUpdate() {
        return template.boundSetOps(PHOTOS_TO_UPDATE_SET);
    }

    public BoundSetOperations<String, String> photoCommentsToUpdate() {
        return template.boundSetOps(PHOTO_COMMENTS_TO_UPDATE_SET);
    }

}
