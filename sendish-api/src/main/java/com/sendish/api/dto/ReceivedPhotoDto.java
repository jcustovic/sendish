package com.sendish.api.dto;

public class ReceivedPhotoDto extends PhotoDto {

    private Boolean like;
    private Boolean report;
    private Boolean opened;

    public Boolean getLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

    public Boolean getReport() {
        return report;
    }

    public void setReport(Boolean report) {
        this.report = report;
    }

    public Boolean getOpened() {
        return opened;
    }

    public void setOpened(Boolean opened) {
        this.opened = opened;
    }

}
