package com.sendish.api.redis.dto;

import java.io.Serializable;

public class CommentStatisticsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long likeCount;
    private Long dislikeCount;

    public CommentStatisticsDto(Long likeCount, Long dislikeCount) {
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public Long getDislikeCount() {
        return dislikeCount;
    }

}
