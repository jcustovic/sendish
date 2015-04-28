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
	private String rankScore;
	private Long totalLikes;
	private Long totalDislikes;
	private Long citiesCount;
	private Long unseenPhotoCount;
	private Long unreadInboxItemCount;
	private Long dailySendLimitLeft;
	private Boolean emailRegistration;
	private Boolean newActivities;

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

	public String getRankScore() {
		return rankScore;
	}

	public void setRankScore(String rankScore) {
		this.rankScore = rankScore;
	}

	public Long getTotalLikes() {
		return totalLikes;
	}

	public void setTotalLikes(Long totalLikes) {
		this.totalLikes = totalLikes;
	}

	public Long getTotalDislikes() {
		return totalDislikes;
	}

	public void setTotalDislikes(Long totalDislikes) {
		this.totalDislikes = totalDislikes;
	}

	public Long getCitiesCount() {
		return citiesCount;
	}

	public void setCitiesCount(Long citiesCount) {
		this.citiesCount = citiesCount;
	}

	public Long getUnseenPhotoCount() {
		return unseenPhotoCount;
	}

	public void setUnseenPhotoCount(Long unseenPhotoCount) {
		this.unseenPhotoCount = unseenPhotoCount;
	}

	public Long getDailySendLimitLeft() {
		return dailySendLimitLeft;
	}

	public void setDailySendLimitLeft(Long dailySendLimitLeft) {
		this.dailySendLimitLeft = dailySendLimitLeft;
	}

	public Boolean getEmailRegistration() {
		return emailRegistration;
	}

	public void setEmailRegistration(Boolean emailRegistration) {
		this.emailRegistration = emailRegistration;
	}

	public Boolean getNewActivities() {
		return newActivities;
	}

	public void setNewActivities(Boolean newActivities) {
		this.newActivities = newActivities;
	}

	public Long getUnreadInboxItemCount() {
		return unreadInboxItemCount;
	}

	public void setUnreadInboxItemCount(Long unreadInboxItemCount) {
		this.unreadInboxItemCount = unreadInboxItemCount;
	}

}
