package com.sendish.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sendish.api.redis.KeyUtils;

@Service
public class RankingServiceImpl {
	
	private static final double NEW_SENDISH_POINTS = 10;
	private static final double LIKED_PHOTO_POINTS = 2;
	private static final double DISLIKED_PHOTO_POINTS = -4;
	private static final double REPORTED_PHOTO_POINTS = -5;
	private static final double LIKED_COMMENT_POINTS = 1;
	private static final double DISLIKED_COMMENT_POINTS = -2;
	
	private BoundZSetOperations<String, String> globalLeaderboard;

    @Autowired
    public RankingServiceImpl(StringRedisTemplate template) {
        globalLeaderboard = template.boundZSetOps(KeyUtils.globalRanking());
    }
    
    @Async
    public void addPointsForNewSendish(Long userId) {
    	increaseScore(userId, NEW_SENDISH_POINTS);
    }
    
    @Async
    public void addPointsForLikedPhoto(Long userId) {
    	increaseScore(userId, LIKED_PHOTO_POINTS);
    }
    
    @Async
    public void removePointsForDislikedPhoto(Long userId) {
    	increaseScore(userId, DISLIKED_PHOTO_POINTS);
    }
    
    @Async
    public void removePointsForReportedPhoto(Long userId) {
    	increaseScore(userId, REPORTED_PHOTO_POINTS);
    }
    
    @Async
    public void addPointsForLikedComment(Long userId) {
    	increaseScore(userId, LIKED_COMMENT_POINTS);
    }
    
    @Async
    public void removePointsForDislikedComment(Long userId) {
    	increaseScore(userId, DISLIKED_COMMENT_POINTS);
    }

	private void increaseScore(Long userId, double points) {
		globalLeaderboard.incrementScore(userId.toString(), points);
	}

}
