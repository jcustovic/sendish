package com.sendish.api.social.connect;

import com.sendish.api.social.connect.util.ConnectionDataUtils;
import com.sendish.repository.UserSocialConnectionRepository;
import com.sendish.repository.model.jpa.UserSocialConnection;
import com.sendish.repository.model.jpa.UserSocialConnectionId;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class JpaConnectionRepository implements ConnectionRepository {

    private final transient Long                     userId;

    private final transient UserSocialConnectionRepository userSocialConnectionRepository;

    private final transient ConnectionFactoryLocator connectionFactoryLocator;

    private final transient TextEncryptor            textEncryptor;

    public JpaConnectionRepository(final Long p_userId, final UserSocialConnectionRepository p_userSocialConnectionRepository, final ConnectionFactoryLocator p_conFactoryLocator,
                                   final TextEncryptor p_textEncryptor) {
        userId = p_userId;
        userSocialConnectionRepository = p_userSocialConnectionRepository;
        connectionFactoryLocator = p_conFactoryLocator;
        textEncryptor = p_textEncryptor;
    }

    @Override
    public final MultiValueMap<String, Connection<?>> findAllConnections() {
        final List<UserSocialConnection> userSocialConnections = userSocialConnectionRepository.findByUserId(userId, new Sort("providerId", "rank"));
        final List<Connection<?>> resultList = convert(userSocialConnections);

        final MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<>();
        final Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
        for (final String registeredProviderId : registeredProviderIds) {
            connections.put(registeredProviderId, Collections.<Connection<?>> emptyList());
        }
        for (final Connection<?> connection : resultList) {
            final String providerId = connection.getKey().getProviderId();
            if (connections.get(providerId).size() == 0) {
                connections.put(providerId, new LinkedList<Connection<?>>());
            }
            connections.add(providerId, connection);
        }

        return connections;
    }

    @Override
    public final List<Connection<?>> findConnections(final String p_providerId) {
        final List<UserSocialConnection> userSocialConnections = userSocialConnectionRepository.findByUserIdAndProviderId(userId, p_providerId, new Sort("providerId", "rank"));

        return convert(userSocialConnections);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A> List<Connection<A>> findConnections(final Class<A> p_apiType) {
        final List<?> connections = findConnections(getProviderId(p_apiType));

        return (List<Connection<A>>) connections;
    }

    @Override
    public final MultiValueMap<String, Connection<?>> findConnectionsToUsers(final MultiValueMap<String, String> p_providerUserIds) {
        if ((p_providerUserIds == null) || p_providerUserIds.isEmpty()) {
            throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
        }

        final List<UserSocialConnection> userSocialConnections = userSocialConnectionRepository.findConnectionsToUsers(userId, p_providerUserIds);
        final List<Connection<?>> resultList = convert(userSocialConnections);

        final MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<>();
        for (final Connection<?> connection : resultList) {
            final String providerId = connection.getKey().getProviderId();
            final List<String> userIds = p_providerUserIds.get(providerId);
            List<Connection<?>> connections = connectionsForUsers.get(providerId);
            if (connections == null) {
                connections = new ArrayList<>(userIds.size());
                for (int i = 0; i < userIds.size(); i++) {
                    connections.add(null);
                }
                connectionsForUsers.put(providerId, connections);
            }
            final String providerUserId = connection.getKey().getProviderUserId();
            final int connectionIndex = userIds.indexOf(providerUserId);
            connections.set(connectionIndex, connection);
        }

        return connectionsForUsers;
    }

    @Override
    public final Connection<?> getConnection(final ConnectionKey p_connectionKey) {
        final UserSocialConnection userSocialConnection = userSocialConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(userId, p_connectionKey.getProviderId(),
                p_connectionKey.getProviderUserId());

        if (userSocialConnection == null) {
            throw new NoSuchConnectionException(p_connectionKey);
        }

        return convert(userSocialConnection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A> Connection<A> getConnection(final Class<A> p_apiType, final String p_providerUserId) {
        final String providerId = getProviderId(p_apiType);

        return (Connection<A>) getConnection(new ConnectionKey(providerId, p_providerUserId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A> Connection<A> getPrimaryConnection(final Class<A> p_apiType) {
        final String providerId = getProviderId(p_apiType);
        final Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
        if (connection == null) {
            throw new NotConnectedException(providerId);
        }

        return connection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A> Connection<A> findPrimaryConnection(final Class<A> p_apiType) {
        final String providerId = getProviderId(p_apiType);

        return (Connection<A>) findPrimaryConnection(providerId);
    }

    @Override
    @Transactional
    public final void addConnection(final Connection<?> p_connection) {
        final ConnectionData data = p_connection.createData();

        final UserSocialConnectionId connectionId = new UserSocialConnectionId(userId, data.getProviderId(), data.getProviderUserId());

        if (userSocialConnectionRepository.findOne(connectionId) != null) {
            throw new DuplicateConnectionException(p_connection.getKey());
        }

        final Integer rank = userSocialConnectionRepository.getRankByUserIdAndProviderId(userId, data.getProviderId());
        final UserSocialConnection userSocialConnection = new UserSocialConnection();
        convert(userSocialConnection, data, connectionId);
        userSocialConnection.setRank(rank);

        userSocialConnectionRepository.save(userSocialConnection);
    }

    @Override
    public final void updateConnection(final Connection<?> p_connection) {
        final ConnectionData data = p_connection.createData();

        final UserSocialConnectionId connectionId = new UserSocialConnectionId(userId, data.getProviderId(), data.getProviderUserId());
        final UserSocialConnection userSocialConnection = userSocialConnectionRepository.findOne(connectionId);
        convert(userSocialConnection, data, connectionId);
        userSocialConnectionRepository.save(userSocialConnection);
    }

    @Override
    public final void removeConnections(final String p_providerId) {
        userSocialConnectionRepository.deleteByUserIdAndProviderId(userId, p_providerId);
    }

    @Override
    public final void removeConnection(final ConnectionKey p_connectionKey) {
        final UserSocialConnectionId connectionId = new UserSocialConnectionId(userId, p_connectionKey.getProviderId(), p_connectionKey.getProviderUserId());
        userSocialConnectionRepository.delete(connectionId);
    }

    private List<Connection<?>> convert(final List<UserSocialConnection> p_userSocialConnections) {
        final LinkedList<Connection<?>> connections = new LinkedList<>();
        for (final UserSocialConnection userSocialConnection : p_userSocialConnections) {
            connections.add(convert(userSocialConnection));
        }

        return connections;
    }

    private Connection<?> convert(final UserSocialConnection p_userSocialConnection) {
        final ConnectionData connectionData = ConnectionDataUtils.mapConnectionData(p_userSocialConnection, textEncryptor);
        final ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());

        return connectionFactory.createConnection(connectionData);
    }

    private void convert(final UserSocialConnection p_userSocialConnection, final ConnectionData p_data, final UserSocialConnectionId p_connectionId) {
        p_userSocialConnection.setUserId(p_connectionId.getUserId());
        p_userSocialConnection.setProviderId(p_connectionId.getProviderId());
        p_userSocialConnection.setProviderUserId(p_connectionId.getProviderUserId());
        p_userSocialConnection.setDisplayName(p_data.getDisplayName());
        p_userSocialConnection.setProfileUrl(p_data.getProfileUrl());
        p_userSocialConnection.setImageUrl(p_data.getImageUrl());
        p_userSocialConnection.setAccessToken(encrypt(p_data.getAccessToken()));
        p_userSocialConnection.setSecret(encrypt(p_data.getSecret()));
        p_userSocialConnection.setRefreshToken(encrypt(p_data.getRefreshToken()));
        p_userSocialConnection.setExpireTime(p_data.getExpireTime());
    }

    private String encrypt(final String p_text) {
        if (textEncryptor == null) {
            return p_text;
        } else {
            return textEncryptor.encrypt(p_text);
        }
    }

    private <A> String getProviderId(final Class<A> p_apiType) {
        return connectionFactoryLocator.getConnectionFactory(p_apiType).getProviderId();
    }

    private Connection<?> findPrimaryConnection(final String p_providerId) {
        final List<UserSocialConnection> userSocialConnections = userSocialConnectionRepository.findByUserIdAndProviderIdAndRank(userId, p_providerId, 1);
        final List<Connection<?>> connections = convert(userSocialConnections);
        if (connections.isEmpty()) {
            return null;
        } else {
            return connections.get(0);
        }
    }

}
