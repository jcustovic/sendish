package com.sendish.api.statistics;

public interface DBStatisticsSynchronizer {

    void syncUserStat(Long userId);

    void syncPhotoStat(Long photoId);

    void syncPhotoCommentStat(Long photoCommentId);

}
