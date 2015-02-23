package com.sendish.api.distributor;

import com.sendish.api.redis.KeyUtils;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@Service
public class RedisBasedDistributorImpl implements PhotoDistributor {

    public static final int USER_LOCK_EXPIRE_IN_SECONDS = 60;

    @Autowired
    private UserPool userPool;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PhotoServiceImpl photoService;

    private StringRedisTemplate redisTemplate;

    @Autowired
    public RedisBasedDistributorImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * STEP 1: Try to lock (with timeout 60s)
     * STEP 2: Has user received a photo?
     *
     * STEP 3a: Yes: Unlock and try next user!
     * STEP 3b: No : Can we send to that user (check all conditions; limits, already received etc.)
     *
     * STEP 4a: No : Remove the user from the list because some limit exceeded!
     * STEP 4b: Yes: Send photo, remove user from the list and return true
     *
     * NOTE: If we don't find any user return false!
     */
    @Override
    public PhotoReceiver sendPhoto(Long photoId) {
        // TODO: Implement smart offset and grab next 'n' if we don't find a matching user in the first 'n'
        Collection<String> users = userPool.getNext(10);
        Iterator<String> iterator = users.iterator();
        while (iterator.hasNext()) {
            String userIdString = iterator.next();
            Long userId = Long.valueOf(userIdString);
            Photo photo = photoService.findOne(photoId);
            
            if (photo.getUser().getId().equals(userId)) {
            	continue;
            } else if (lockUser(userId)) {
                if (photoService.hasAlreadyReceivedPhoto(photoId, userId)) {
                    unlockUser(userId);
                } else if (userService.canReceivePhoto(userId)) {
                    PhotoReceiver photoReceiver = photoService.sendPhotoToUser(photoId, userId);
                    userPool.remove(userIdString);

                    return photoReceiver;
                } else {
                	unlockUser(userId);
                }
            }
        }

        return null;
    }

    private void unlockUser(Long userId) {
        redisTemplate.delete(KeyUtils.usersPoolLock(userId));
    }

    private boolean lockUser(Long userId) {
        // TODO: Replace with "SET key value [EX seconds] [PX milliseconds] [NX|XX]" (Starting with Redis 2.6.12)
        BoundValueOperations<String, String> userLock = userLock(userId);
        Boolean result = userLock.setIfAbsent("L");
        if (result) {
            userLock.expire(USER_LOCK_EXPIRE_IN_SECONDS, TimeUnit.SECONDS);
        }

        return result;
    }

    public BoundValueOperations<String, String> userLock(long userId) {
        return redisTemplate.boundValueOps(KeyUtils.usersPoolLock(userId));
    }

}
