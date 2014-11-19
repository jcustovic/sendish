package com.sendish.api.social.connect.util;

import com.sendish.repository.model.jpa.UserSocialConnection;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.ConnectionData;

public final class ConnectionDataUtils {

    public static ConnectionData mapConnectionData(final UserSocialConnection p_userSocialConnection, TextEncryptor p_textEncryptor) {
        return new ConnectionData(p_userSocialConnection.getProviderId(), p_userSocialConnection.getProviderUserId(), p_userSocialConnection.getDisplayName(),
                p_userSocialConnection.getProfileUrl(), p_userSocialConnection.getImageUrl(), decrypt(p_userSocialConnection.getAccessToken(), p_textEncryptor),
                decrypt(p_userSocialConnection.getSecret(), p_textEncryptor), decrypt(p_userSocialConnection.getRefreshToken(), p_textEncryptor),
                expireTime(p_userSocialConnection.getExpireTime()));
    }

    private static String decrypt(final String p_encryptedText, TextEncryptor p_textEncryptor) {
        if (p_textEncryptor == null) {
            return p_encryptedText;
        } else {
            return p_textEncryptor.decrypt(p_encryptedText);
        }
    }

    private static Long expireTime(final Long p_expireTime) {
        if ((p_expireTime == null) || (p_expireTime == 0)) {
            return null;
        } else {
            return p_expireTime;
        }
    }

}
