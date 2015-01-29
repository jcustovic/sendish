package com.sendish.api.distributor;

public class UserWithScore {

    private String userId;
    private Long score;

    public UserWithScore(String userId, Long score) {
        this.userId = userId;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public Long getScore() {
        return score;
    }

}
