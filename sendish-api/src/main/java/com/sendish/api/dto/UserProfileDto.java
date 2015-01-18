package com.sendish.api.dto;

import java.math.BigDecimal;
import java.util.Date;

public class UserProfileDto extends BaseEntityDto {

    private static final long serialVersionUID = 1L;

    private String nick;
    private String lastPlace;
    private BigDecimal lastLng;
    private BigDecimal lastLat;
    private Date lastLocationTime;
    private String rank;
    private Integer totalLikes;
    private Integer totalDislikes;
    private Integer citiesCount;
    private Integer unseenPhotoCount;
    private Boolean emailRegistration;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getLastPlace() {
        return lastPlace;
    }

    public void setLastPlace(String lastPlace) {
        this.lastPlace = lastPlace;
    }

    public BigDecimal getLastLng() {
        return lastLng;
    }

    public void setLastLng(BigDecimal lastLng) {
        this.lastLng = lastLng;
    }

    public BigDecimal getLastLat() {
        return lastLat;
    }

    public void setLastLat(BigDecimal lastLat) {
        this.lastLat = lastLat;
    }

    public Date getLastLocationTime() {
        return lastLocationTime;
    }

    public void setLastLocationTime(Date lastLocationTime) {
        this.lastLocationTime = lastLocationTime;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Integer totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Integer getTotalDislikes() {
        return totalDislikes;
    }

    public void setTotalDislikes(Integer totalDislikes) {
        this.totalDislikes = totalDislikes;
    }

    public Integer getCitiesCount() {
        return citiesCount;
    }

    public void setCitiesCount(Integer citiesCount) {
        this.citiesCount = citiesCount;
    }

    public Integer getUnseenPhotoCount() {
		return unseenPhotoCount;
	}

	public void setUnseenPhotoCount(Integer unseenPhotoCount) {
		this.unseenPhotoCount = unseenPhotoCount;
	}

	public Boolean getEmailRegistration() {
        return emailRegistration;
    }

    public void setEmailRegistration(Boolean emailRegistration) {
        this.emailRegistration = emailRegistration;
    }

}
