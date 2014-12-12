package com.sendish.api.dto;

import java.io.Serializable;

public class UserRankDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String rank;
    private Long points;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

}
