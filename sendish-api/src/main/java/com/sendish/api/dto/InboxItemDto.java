package com.sendish.api.dto;

public class InboxItemDto extends BaseEntityDto {

    private static final long serialVersionUID = 1L;

    private String shortTitle;
    private String title;
    private String message;
    private String imageUuid;
    private String url;
    private String urlText;
    private Boolean read;
    private String timeAgo;

    // Getters & setters

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUuid() {
        return imageUuid;
    }

    public void setImageUuid(String imageUuid) {
        this.imageUuid = imageUuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUrlText() {
		return urlText;
	}

	public void setUrlText(String urlText) {
		this.urlText = urlText;
	}

	public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

}
