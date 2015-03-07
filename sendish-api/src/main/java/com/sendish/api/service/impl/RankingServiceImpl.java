package com.sendish.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sendish.api.dto.UserRankDto;
import com.sendish.api.redis.KeyUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;

@Service
public class RankingServiceImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(RankingServiceImpl.class);
	
	private static final double NEW_SENDISH_POINTS = 10;
	private static final double LIKED_PHOTO_POINTS = 2;
	private static final double DISLIKED_PHOTO_POINTS = -4;
	private static final double REPORTED_PHOTO_POINTS = -5;
	private static final double LIKED_COMMENT_POINTS = 1;
	private static final double DISLIKED_COMMENT_POINTS = -2;
	
	@Autowired
	private UserRepository userRepository;
	
	private BoundZSetOperations<String, String> globalLeaderboard;

    @Autowired
    public RankingServiceImpl(StringRedisTemplate template) {
        globalLeaderboard = template.boundZSetOps(KeyUtils.globalRanking());
    }
    
    @Cacheable("com.sendish.api.service.impl.RankingServiceImpl.getFromTop")
    public List<UserRankDto> getFromTop(long start, int end) {
    	Set<TypedTuple<String>> topUsers = globalLeaderboard.reverseRangeWithScores(start, end);
    	List<UserRankDto> usersRank = new ArrayList<>(topUsers.size());
    	
    	int i = 0;
    	for (TypedTuple<String> topUser : topUsers) {
			User user = userRepository.findOne(Long.valueOf(topUser.getValue()));
			String username;
			if (user.getDetails().getCurrentCity() == null) {
				username = "Noname";
			} else {
				username = UserUtils.getDisplayNameWithCity(user);
			}
			usersRank.add(new UserRankDto(user.getId(), username, String.valueOf(++i) + ".", String.valueOf(topUser.getScore().longValue())));
		}

    	return usersRank;
    }
    
    public Long getRank(Long userId) {
    	Long rank = globalLeaderboard.reverseRank(userId.toString());
    	if (rank == null) {
    		return null;
    	} else {
    		return rank + 1;
    	}
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
		try {
			globalLeaderboard.incrementScore(userId.toString(), points);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}		

}
