package com.sendish.api.dto.admin;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class CreateInboxMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private MultipartFile image;

    @NotEmpty
    @Size(max = 64)
    private String shortTitle;

    @NotEmpty
    @Size(max = 256)
    private String title;

    @NotEmpty
    private String message;

    @URL
    private String url;

    @Size(max = 32)
    private String urlText;

    // Getters & setters

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

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

}
