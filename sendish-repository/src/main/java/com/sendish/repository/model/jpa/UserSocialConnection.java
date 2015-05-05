package com.sendish.repository.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@IdClass(UserSocialConnectionId.class)
@Table(name = "user_social_connection", uniqueConstraints = { @UniqueConstraint(name = "accountId_providerId_accessToken_UNIQUE", columnNames = { "usc_user_id",
        "usc_provider_id", "usc_rank" }) })
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserSocialConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int PROVIDER_ID_MAX_LENGTH      = 255;
    public static final int PROVIDER_USER_ID_MAX_LENGTH = 255;
    public static final int DISPLAY_NAME_MAX_LENGTH     = 255;
    public static final int PROFILE_URL_MAX_LENGTH      = 512;
    public static final int IMAGE_URL_MAX_LENGTH        = 512;
    public static final int ACCESS_TOKEN_MAX_LENGTH     = 512;
    public static final int SECRET_MAX_LENGTH           = 255;
    public static final int REFRESH_TOKEN_MAX_LENGTH    = 512;

    @Id
    @Column(name = "usc_user_id", nullable = false)
    private Long            userId;

    @Id
    @Column(name = "usc_provider_id", length = PROVIDER_ID_MAX_LENGTH, nullable = false)
    private String          providerId;

    @Id
    @Column(name = "usc_provider_user_id", length = PROVIDER_USER_ID_MAX_LENGTH, nullable = false)
    private String          providerUserId;

    @Column(name = "usc_rank", nullable = false)
    private Integer         rank;

    @Column(name = "usc_display_name", length = DISPLAY_NAME_MAX_LENGTH)
    private String          displayName;

    @Column(name = "usc_profile_url", length = PROFILE_URL_MAX_LENGTH)
    private String          profileUrl;

    @Column(name = "usc_image_url", length = IMAGE_URL_MAX_LENGTH)
    private String          imageUrl;

    @Column(name = "usc_access_token", length = ACCESS_TOKEN_MAX_LENGTH, nullable = false)
    private String          accessToken;

    @Column(name = "usc_secret", length = SECRET_MAX_LENGTH)
    private String          secret;

    @Column(name = "usc_refresh_token", length = REFRESH_TOKEN_MAX_LENGTH)
    private String          refreshToken;

    @Column(name = "usc_expire_time")
    private Long            expireTime;
    
    @Column(name = "usc_modified_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime modifiedDate;
    

    @PrePersist
    @PreUpdate
    public final void updateModifyDate() {
        modifiedDate = DateTime.now();
    }

    // Getters & setters

    public final Long getUserId() {
        return userId;
    }

    public final void setUserId(final Long p_userId) {
        userId = p_userId;
    }

    public final String getProviderId() {
        return providerId;
    }

    public final void setProviderId(final String p_providerId) {
        providerId = p_providerId;
    }

    public final String getProviderUserId() {
        return providerUserId;
    }

    public final void setProviderUserId(final String p_providerUserId) {
        providerUserId = p_providerUserId;
    }

    public final Integer getRank() {
        return rank;
    }

    public final void setRank(final Integer p_rank) {
        rank = p_rank;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final void setDisplayName(final String p_displayName) {
        displayName = p_displayName;
    }

    public final String getProfileUrl() {
        return profileUrl;
    }

    public final void setProfileUrl(final String p_profileUrl) {
        profileUrl = p_profileUrl;
    }

    public final String getImageUrl() {
        return imageUrl;
    }

    public final void setImageUrl(final String p_imageUrl) {
        imageUrl = p_imageUrl;
    }

    public final String getAccessToken() {
        return accessToken;
    }

    public final void setAccessToken(final String p_accessToken) {
        accessToken = p_accessToken;
    }

    public final String getSecret() {
        return secret;
    }

    public final void setSecret(final String p_secret) {
        secret = p_secret;
    }

    public final String getRefreshToken() {
        return refreshToken;
    }

    public final void setRefreshToken(final String p_refreshToken) {
        refreshToken = p_refreshToken;
    }

    public final Long getExpireTime() {
        return expireTime;
    }

    public final void setExpireTime(final Long p_expireTime) {
        expireTime = p_expireTime;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

}
