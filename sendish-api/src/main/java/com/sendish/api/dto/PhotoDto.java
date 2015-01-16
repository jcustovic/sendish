package com.sendish.api.dto;

public class PhotoDto extends BaseEntityDto {

    private static final long serialVersionUID = 1L;

    private String originLocation;
    private String description;
    private String timeAgo;
    private String uuid;
    private Long likeCount;
    private Long cityCount;
    private Long commentCount;

    public String getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCityCount() {
        return cityCount;
    }

    public void setCityCount(Long cityCount) {
        this.cityCount = cityCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

}
