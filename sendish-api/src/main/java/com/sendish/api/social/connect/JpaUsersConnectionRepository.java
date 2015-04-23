package com.sendish.api.social.connect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sendish.repository.UserSocialConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

public class JpaUsersConnectionRepository implements UsersConnectionRepository {

    @Autowired
    private transient UserSocialConnectionRepository userSocialConnectionRepository;

    private final transient ConnectionFactoryLocator connectionFactoryLocator;

    private final transient TextEncryptor            textEncryptor;

    private transient ConnectionSignUp               connectionSignUp;

    public JpaUsersConnectionRepository(final ConnectionFactoryLocator p_conFactoryLocator, final TextEncryptor p_textEncryptor, final ConnectionSignUp p_connectionSignUp) {
        super();
        this.connectionFactoryLocator = p_conFactoryLocator;
        this.textEncryptor = p_textEncryptor;
        this.connectionSignUp = p_connectionSignUp;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public final List<String> findUserIdsWithConnection(final Connection<?> p_connection) {
        final ConnectionKey key = p_connection.getKey();

        final List<Long> localUserIds = userSocialConnectionRepository.findUserId(key.getProviderId(), key.getProviderUserId());
        if (localUserIds.isEmpty()) {
            final String newUserId = connectionSignUp.execute(p_connection);
            if (newUserId != null) {
                createConnectionRepository(newUserId).addConnection(p_connection);
                return Arrays.asList(newUserId);
            }
        }

        final List<String> stringList = new ArrayList<>(localUserIds.size());
        for (final Long userId : localUserIds) {
            stringList.add(String.valueOf(userId));
        }

        return stringList;
    }

    @Override
    public final Set<String> findUserIdsConnectedTo(final String p_providerId, final Set<String> p_providerUserIds) {
        final Set<Long> localUserIds = userSocialConnectionRepository.findUserIdsConnectedTo(p_providerId, p_providerUserIds);

        final Set<String> stringSet = new HashSet<>(localUserIds.size());
        for (final Long userId : localUserIds) {
            stringSet.add(String.valueOf(userId));
        }

        return stringSet;
    }

    @Override
    public final ConnectionRepository createConnectionRepository(final String p_userId) {
        if (!StringUtils.hasText(p_userId)) {
            throw new IllegalArgumentException("userId must be provided");
        }

        return new JpaConnectionRepository(Long.valueOf(p_userId), userSocialConnectionRepository, connectionFactoryLocator, textEncryptor);
    }

}
