package com.sendish.api.statistics;


import com.sendish.api.redis.dto.CommentStatisticsDto;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.dto.UserStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.api.service.impl.RankingServiceImpl;
import com.sendish.repository.PhotoCommentRepository;
import com.sendish.repository.PhotoStatisticsRepository;
import com.sendish.repository.UserStatisticsRepository;
import com.sendish.repository.model.jpa.PhotoComment;
import com.sendish.repository.model.jpa.PhotoStatistics;
import com.sendish.repository.model.jpa.UserStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class RedisDBStatisticsSynchronizerScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDBStatisticsSynchronizer.class);

    public static final long SYNC_RATE_INTERVAL_5_MIN = 300000L;

    @Autowired
    private RedisStatisticsRepository statisticsRepository;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    @Autowired
    private RankingServiceImpl rankingService;

    @Autowired
    private PhotoStatisticsRepository photoStatisticsRepository;

    @Autowired
    private PhotoCommentRepository photoCommentRepository;

    @Autowired
    private RedisDBStatisticsSynchronizer redisDBStatisticsSynchronizer;

    @Scheduled(fixedDelay = SYNC_RATE_INTERVAL_5_MIN)
    public void syncChangedUsersStatToDb() {
        BoundSetOperations<String, String> usersToUpdate = redisDBStatisticsSynchronizer.usersToUpdate();
        LOGGER.info("Syncing {} changed users stats...", usersToUpdate.size());
        while (true) {
            String userIdString = usersToUpdate.pop();
            if (userIdString == null) {
                break;
            }
            try {
                syncUserToDb(Long.valueOf(userIdString));
            } catch (NumberFormatException e) {
                LOGGER.error("Expected Long!", e);
            }
        }

        LOGGER.info("Syncing changed users done.");
    }

    @Scheduled(fixedDelay = SYNC_RATE_INTERVAL_5_MIN)
    public void syncChangedPhotosStatToDb() {
        BoundSetOperations<String, String> photosToUpdate = redisDBStatisticsSynchronizer.photosToUpdate();
        LOGGER.info("Syncing {} changed photo stats...", photosToUpdate.size());
        while (true) {
            String photoIdString = photosToUpdate.pop();
            if (photoIdString == null) {
                break;
            }
            try {
                syncPhotoToDb(Long.valueOf(photoIdString));
            } catch (NumberFormatException e) {
                LOGGER.error("Expected Long!", e);
            }
        }

        LOGGER.info("Syncing changed photos done.");
    }

    @Scheduled(fixedDelay = SYNC_RATE_INTERVAL_5_MIN)
    public void syncChangedPhotoCommentStatToDb() {
        BoundSetOperations<String, String> photoCommentsToUpdate = redisDBStatisticsSynchronizer.photoCommentsToUpdate();
        LOGGER.info("Syncing {} changed photo comments stats...", photoCommentsToUpdate.size());
        while (true) {
            String photoCommentIdString = photoCommentsToUpdate.pop();
            if (photoCommentIdString == null) {
                break;
            }
            try {
                syncPhotoCommentToDb(Long.valueOf(photoCommentIdString));
            } catch (NumberFormatException e) {
                LOGGER.error("Expected Long!", e);
            }
        }

        LOGGER.info("Syncing changed photo comments done.");
    }

    private void syncUserToDb(Long userId) {
        UserStatisticsDto userStat = statisticsRepository.getUserStatistics(userId);
        UserStatistics userStatDb = userStatisticsRepository.findOne(userId);
        userStatDb.setCities(userStat.getTotalCityCount().intValue());
        userStatDb.setDislikes(userStat.getTotalDislikeCount().intValue());
        userStatDb.setLikes(userStat.getTotalLikeCount().intValue());
        userStatDb.setReports(userStat.getTotalReportCount().intValue());

        // TODO: Should we move rank update somewhere else?
        Long rank = rankingService.getRank(userId);
        userStatDb.setRank(rank.intValue());

        userStatisticsRepository.save(userStatDb);
    }

    private void syncPhotoToDb(Long photoId) {
        PhotoStatisticsDto photoStat = statisticsRepository.getPhotoStatistics(photoId);
        PhotoStatistics photoStatDb = photoStatisticsRepository.findOne(photoId);
        photoStatDb.setCities(photoStat.getCityCount().intValue());
        photoStatDb.setComments(photoStat.getCommentCount().intValue());
        photoStatDb.setDislikes(photoStat.getDislikeCount().intValue());
        photoStatDb.setLikes(photoStat.getLikeCount().intValue());
        photoStatDb.setReports(photoStat.getReportCount().intValue());

        photoStatisticsRepository.save(photoStatDb);
    }

    private void syncPhotoCommentToDb(Long photoCommentId) {
        CommentStatisticsDto photoCommentStat = statisticsRepository.getCommentStatistics(photoCommentId);
        PhotoComment photoCommentStatDb = photoCommentRepository.findOne(photoCommentId);
        photoCommentStatDb.setDislikes(photoCommentStat.getDislikeCount().intValue());
        photoCommentStatDb.setLikes(photoCommentStat.getLikeCount().intValue());

        photoCommentRepository.save(photoCommentStatDb);
    }

}
