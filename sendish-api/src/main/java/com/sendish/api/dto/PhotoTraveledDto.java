package com.sendish.api.dto;

public class PhotoTraveledDto extends BaseEntityDto {

    private static final long serialVersionUID = 1L;

    private String timeAgo;
    private String location;
    private Boolean liked;

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

}
