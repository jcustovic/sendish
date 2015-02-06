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

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPoolFillerScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPoolFillerScheduler.class);

    private static final long TEN_SECONDS_DELAY = 10000L;
    private static final long MAX_FETCH_SIZE = 1000;
    private static final long MAX_USER_POOL_SIZE = 10000;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private UserPool userPool;

    @Scheduled(fixedDelay = TEN_SECONDS_DELAY)
    public void fillUsersPool() {
        Long poolSize = userPool.getPoolSize();
        if (poolSize < MAX_USER_POOL_SIZE - 100) {
            long neededSize = MAX_USER_POOL_SIZE - userPool.getPoolSize();
            int fetchSize = (int) Math.min(MAX_FETCH_SIZE, neededSize);
            LOGGER.info("Fetching {} users for user pool. Pool size: {}", fetchSize, poolSize);
            DateTime lastUserPhotoReceivedDate = getLastUserPhotoReceivedDate();

            LOGGER.info("Last user received photo timestamp in pool is {}", lastUserPhotoReceivedDate);

            Page<UserDetails> userDetails = userDetailsRepository.searchUsersForSendingPool(lastUserPhotoReceivedDate, fetchSize);
            if (userDetails.hasContent()) {
                List<UserWithScore> usersWithScore = userDetails.getContent().stream()
                        .map(user -> new UserWithScore(user.getUserId().toString(), user.getLastReceivedTime().getMillis()))
                        .collect(Collectors.toList());

                userPool.put(usersWithScore);
            } else {
                LOGGER.info("No users found for for user poll");
            }
        } else {
            LOGGER.info("Skipping user pool fetching because pool is full");
        }
    }

    private DateTime getLastUserPhotoReceivedDate() {
        UserWithScore lastUser = userPool.getLastWithScore();
        if (lastUser == null) {
            return null;
        } else {
            return new DateTime(lastUser.getScore());
        }
    }

}
