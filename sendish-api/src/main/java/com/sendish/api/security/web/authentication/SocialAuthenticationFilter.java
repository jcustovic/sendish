package com.sendish.api.security.web.authentication;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.sendish.api.security.authentication.CustomUserDetailsService;
import com.sendish.api.service.impl.UserSocialConnectionServiceImpl;
import com.sendish.api.service.impl.UsersConnectionServiceImpl;
import com.sendish.api.social.connect.util.ConnectionDataUtils;
import com.sendish.repository.model.jpa.UserSocialConnection;

public class SocialAuthenticationFilter extends GenericFilterBean {

    private static final int OAUTH1_PARAM_LENGTH = 3;
    private static final int OAUTH2_PARAM_LENGTH = 5;

    private static final Logger logger = LoggerFactory.getLogger(SocialAuthenticationFilter.class);

    // ~ Instance fields
    // ================================================================================================

    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private UsersConnectionServiceImpl usersConnectionService;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserSocialConnectionServiceImpl userSocialConnectionService;

    public SocialAuthenticationFilter() {
        super();
    }

    public SocialAuthenticationFilter(final AuthenticationEntryPoint p_authenticationEntryPoint) {
        super();
        authenticationEntryPoint = p_authenticationEntryPoint;
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public final void afterPropertiesSet() {
        Assert.notNull(authenticationEntryPoint, "An AuthenticationEntryPoint is required");
    }

    @Override
    public final void doFilter(final ServletRequest p_req, final ServletResponse p_res, final FilterChain p_chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) p_req;
        final HttpServletResponse response = (HttpServletResponse) p_res;

        final String header = request.getHeader("SocialAuthorization");

        // Check for header
        if (!StringUtils.hasText(header)) {
            logger.debug("SocialAuthorization header not found");
            p_chain.doFilter(request, response);
            return;
        }

        final String[] socialAuth = header.split(":");

        if (socialAuth.length < 2) {
            logger.info("Insufficient SocialAuthorization params");
            authenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException("Insufficient SocialAuthorization params"));
            return;
        }

        if (socialAuth[1].length() > 255) {
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("Max 255 chars allowed for access token"));
            return;
        }

        // facebook, twitter, linkedin etc.
        final String providerId = socialAuth[0];

        final ConnectionFactory<?> connectionFactory;
        try {
            connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId);
        } catch (final IllegalArgumentException e) {
            logger.info("Social connection factory not found for provider " + providerId);
            authenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException("Social connection factory not found for provider " + providerId));
            return;
        }
        final Connection<?> connection = getOAuthConnection(request, response, socialAuth, connectionFactory);
        if (connection == null) {
        	return;
        }

        final List<String> userIds = usersConnectionService.findUserIdsWithConnection(connection);
        if (userIds.size() == 0) {
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("Unable to create user"));
            return;
        } else if (userIds.size() == 1) {
        	usersConnectionService.updateConnection(userIds.get(0), connection);

            final UserDetails user = userDetailsService.loadUserById(Long.valueOf(userIds.get(0)));

            final UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authResult);
        } else {
            logger.error("Found more than one user from connection -> {}", userIds);
        }

        p_chain.doFilter(request, response);
    }

	private Connection<?> getOAuthConnection(final HttpServletRequest request, final HttpServletResponse response, 
			final String[] socialAuth, final ConnectionFactory<?> connectionFactory) throws IOException, ServletException {
		Connection<?> connection = null;
		try {
			if (connectionFactory instanceof OAuth1ConnectionFactory) {
			    if (socialAuth.length != OAUTH1_PARAM_LENGTH) {
			        logger.info("Insufficient SocialAuthorization params for OAuth1");
			        authenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException("Insufficient SocialAuthorization params for OAuth1"));
			        return null;
			    }

                connection = getOAuth1Connection(socialAuth, (OAuth1ConnectionFactory<?>) connectionFactory);
            } else if (connectionFactory instanceof OAuth2ConnectionFactory) {
			    if (socialAuth.length != OAUTH2_PARAM_LENGTH) {
			        logger.info("Insufficient SocialAuthorization params for OAuth2");
			        authenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException("Insufficient SocialAuthorization params for OAuth2"));
			        return null;
			    }

                connection = getOAuth2Connection(socialAuth, (OAuth2ConnectionFactory<?>) connectionFactory);
			} else {
			    authenticationEntryPoint.commence(request, response, new ProviderNotFoundException("Connection provider not supported"));
			}
		} catch (final NotAuthorizedException e) {
            logger.info(e.getMessage());
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid OAuth access token."));
        }
		
		return connection;
	}

    private Connection<?> getOAuth1Connection(String[] socialAuth, OAuth1ConnectionFactory<?> connectionFactory) {
        Connection<?> connection;

        OAuthToken oAuthToken = new OAuthToken(socialAuth[1], socialAuth[2]);

        UserSocialConnection userSocialConnection = userSocialConnectionService.findByOAuth1(socialAuth[0], oAuthToken.getValue(), oAuthToken.getSecret());
        if (userSocialConnection == null || needsUpdate(userSocialConnection)) {
            connection = ((OAuth1ConnectionFactory<?>) connectionFactory).createConnection(oAuthToken);
        } else {
            ConnectionData connectionData = ConnectionDataUtils.mapConnectionData(userSocialConnection, null);
            connection = ((OAuth1ConnectionFactory<?>) connectionFactory).createConnection(connectionData);
        }

        return connection;
    }

    private Connection<?> getOAuth2Connection(String[] socialAuth, OAuth2ConnectionFactory<?> connectionFactory) {
        Connection<?> connection;

        Long expiry = null;
        final String expiryString = socialAuth[4];
        if (StringUtils.hasText(expiryString)) {
            expiry = Long.valueOf(expiryString);
        }
        // String scope = socialAuth[2];
        AccessGrant accessGrant = new AccessGrant(socialAuth[1], null, socialAuth[3], expiry);
        UserSocialConnection userSocialConnection = userSocialConnectionService.findByOAuth2(socialAuth[0], accessGrant.getAccessToken(), accessGrant.getRefreshToken());
        if (userSocialConnection == null || needsUpdate(userSocialConnection) || isExpired(userSocialConnection)) {
            connection = ((OAuth2ConnectionFactory<?>) connectionFactory).createConnection(accessGrant);
        } else {
            ConnectionData connectionData = ConnectionDataUtils.mapConnectionData(userSocialConnection, null);
            connection = ((OAuth2ConnectionFactory<?>) connectionFactory).createConnection(connectionData);
        }

        return connection;
    }

    private boolean isExpired(UserSocialConnection userSocialConnection) {
        return userSocialConnection.getExpireTime() != null && System.currentTimeMillis() >= userSocialConnection.getExpireTime();
    }

    /**
     * Mark connection needs update if its last update was more than 2 days ago.
     *
     * @param userSocialConnection
     * @return
     */
    private boolean needsUpdate(UserSocialConnection userSocialConnection) {
        return userSocialConnection.getModifiedDate().plusDays(2).isBefore(DateTime.now());
    }

    // Getters & setters

	public void setConnectionFactoryLocator(ConnectionFactoryLocator connectionFactoryLocator) {
		this.connectionFactoryLocator = connectionFactoryLocator;
	}

	public void setUsersConnectionService(UsersConnectionServiceImpl usersConnectionService) {
		this.usersConnectionService = usersConnectionService;
	}

	public void setUserDetailsService(CustomUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public void setUserSocialConnectionService(UserSocialConnectionServiceImpl userSocialConnectionService) {
		this.userSocialConnectionService = userSocialConnectionService;
	}

}