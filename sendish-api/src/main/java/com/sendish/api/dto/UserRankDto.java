package com.sendish.api.dto;

import java.io.Serializable;

public class UserRankDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String name;
    private String rank;
    private Long points;
    
    public UserRankDto(Long userId, String name, String rank, Long points) {
		super();
		this.userId = userId;
		this.name = name;
		this.rank = rank;
		this.points = points;
	}
    
    // Getters & setters

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

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
