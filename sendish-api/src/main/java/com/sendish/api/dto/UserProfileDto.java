package com.sendish.api.dto;

public class UserProfileDto extends BaseEntityDto {

    private static final long serialVersionUID = 1L;

    private String nick;
    private String city;
    private String country;
    private Double lastLng;
    private Double lastLat;
    private Integer rank;
    private Integer totalLikes;
    private Integer totalDislikes;
    private Integer citiesCount;
    private Boolean signedWithEmail;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getLastLng() {
        return lastLng;
    }

    public void setLastLng(Double lastLng) {
        this.lastLng = lastLng;
    }

    public Double getLastLat() {
        return lastLat;
    }

    public void setLastLat(Double lastLat) {
        this.lastLat = lastLat;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
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

    public Boolean getSignedWithEmail() {
        return signedWithEmail;
    }

    public void setSignedWithEmail(Boolean signedWithEmail) {
        this.signedWithEmail = signedWithEmail;
    }

}
