package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "inbox_message")
@SequenceGenerator(name = "idSequence", sequenceName = "inbox_message_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "im_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class InboxMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "im_short_title", nullable = false, length = 64)
    private String shortTitle;

    @Column(name = "im_title", nullable = false, length = 256)
    private String title;

    @Column(name = "im_message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "im_url", length = 256)
    private String url;

    @Column(name = "im_url_text", length = 32)
    private String urlText;

    @ManyToOne(optional = false)
    @JoinColumn(name = "im_image_id", nullable = false)
    private Image image;

    @Column(name = "im_created_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;

    @PrePersist
    public final void markCreatedDate() {
        createdDate = DateTime.now();
    }

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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

}
