package com.sendish.api.distributor;

public class UserWithScore {

    private Long userId;
    private Long score;

    public UserWithScore(Long userId, Long score) {
        this.userId = userId;
        this.score = score;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getScore() {
        return score;
    }

}
