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
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.UserRankDto;
import com.sendish.api.redis.KeyUtils;
import com.sendish.api.util.UserUtils;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;

@Service
@Transactional
public class RankingServiceImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(RankingServiceImpl.class);
	
	private static final double NEW_SENDISH_POINTS = 5;
	private static final double LIKED_PHOTO_POINTS = 2;
	private static final double LIKED_COMMENT_POINTS = 1;
	
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
			
			if (topUser.getScore() > 0 && !user.getUsername().startsWith("fake")) {
				usersRank.add(new UserRankDto(user.getId(), username, String.valueOf(++i) + ".", String.valueOf(topUser.getScore().longValue())));	
			}
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

	public Long getScore(Long userId) {
		Double rank = globalLeaderboard.score(userId.toString());

		if (rank == null) {
			return null;
		} else {
			return rank.longValue();
		}
	}
    
    @Async
    public void addPointsForNewSendish(User user) {
    	if (user.isUserActive()) {
    		addScore(user.getId(), NEW_SENDISH_POINTS);
    	}
    }
    
    @Async
    public void addPointsForLikedPhoto(User user) {
    	if (user.isUserActive()) {
    		addScore(user.getId(), LIKED_PHOTO_POINTS);
    	}
    }

	@Async
	public void addPointsForLikedComment(User user) {
		if (user.isUserActive()) {
			addScore(user.getId(), LIKED_COMMENT_POINTS);
		}
	}

	private void addScore(Long userId, double points) {
		try {
			globalLeaderboard.incrementScore(userId.toString(), points);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}		

}
