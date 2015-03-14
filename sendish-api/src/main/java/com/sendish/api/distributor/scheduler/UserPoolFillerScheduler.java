package com.sendish.api.distributor.scheduler;

import com.sendish.api.distributor.UserPool;
import com.sendish.api.distributor.UserWithScore;
import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.model.jpa.UserDetails;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPoolFillerScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPoolFillerScheduler.class);

    private static final long HALF_MINUTE_DELAY = 30000L;
    private static final long MAX_FETCH_SIZE = 1000;
    private static final long MAX_USER_POOL_SIZE = 10000;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private UserPool userPool;

    @Scheduled(fixedDelay = HALF_MINUTE_DELAY)
    @Transactional
    public void fillUsersPool() {
        Long poolSize = userPool.getPoolSize();
        if (poolSize < MAX_USER_POOL_SIZE - 100) {
            long neededSize = MAX_USER_POOL_SIZE - poolSize;
            int fetchSize = (int) Math.min(MAX_FETCH_SIZE, neededSize);
            LOGGER.info("Fetching {} (needed: {}) users for user pool. Current users in pool: {}.", fetchSize, neededSize, poolSize);
            
            DateTime latestUserPhotoReceivedDate = getLatestUserPhotoReceivedDate();
            LOGGER.info("Newset user received photo timestamp in pool is {}", latestUserPhotoReceivedDate);
            DateTime oldestUserPhotoReceivedDate = getOldestUserPhotoReceivedDate();
            LOGGER.info("Oldest user received photo timestamp in pool is {}", oldestUserPhotoReceivedDate);

            // TODO: Only return userId and lastReceivedTime! Save all the joins and hibernate city and what not selections!
            Page<UserDetails> userDetails = userDetailsRepository.searchUsersForSendingPool(latestUserPhotoReceivedDate, fetchSize);
            if (userDetails.hasContent()) {
                List<UserWithScore> usersWithScore = userDetails.getContent().stream()
                        .map(user -> new UserWithScore(user.getUserId().toString(), (user.getLastReceivedTime() == null) ? 0 : user.getLastReceivedTime().getMillis()))
                        .collect(Collectors.toList());

                LOGGER.info("Putting {} users to pool...", userDetails.getNumberOfElements());
                userPool.put(usersWithScore);
            } else {
                LOGGER.info("No users found for user pool");
            }
            
            if (oldestUserPhotoReceivedDate != null) {
            	checkIfWeHaveSomeOldUsers(oldestUserPhotoReceivedDate.minusMinutes(10));	
            }
        } else {
            LOGGER.info("Skipping user pool fetching because pool is full");
        }
    }

    private void checkIfWeHaveSomeOldUsers(DateTime oldestUserPhotoReceivedDate) {
        Page<UserDetails> userDetails = userDetailsRepository.searchOldUsersForSendingPool(oldestUserPhotoReceivedDate, 100);
        if (userDetails.hasContent()) {
            List<UserWithScore> usersWithScore = userDetails.getContent().stream()
                    .map(user -> new UserWithScore(user.getUserId().toString(), (user.getLastReceivedTime() == null) ? 0 : user.getLastReceivedTime().getMillis()))
                    .collect(Collectors.toList());
            
            LOGGER.info("Putting {} old users (not active for a while) to pool...", userDetails.getNumberOfElements());
            userPool.put(usersWithScore);
        } else {
            LOGGER.info("No old users found for user pool");
        }
	}

	private DateTime getLatestUserPhotoReceivedDate() {
        UserWithScore lastUser = userPool.getLastWithScore();
        if (lastUser == null || lastUser.getScore() == 0) {
            return null;
        } else {
            return new DateTime(lastUser.getScore());
        }
    }
	
	private DateTime getOldestUserPhotoReceivedDate() {
        UserWithScore firstUser = userPool.getFirstWithScore();
        if (firstUser == null || firstUser.getScore() == 0) {
            return null;
        } else {
            return new DateTime(firstUser.getScore());
        }
    }

}
