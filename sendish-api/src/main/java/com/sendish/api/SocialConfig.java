package com.sendish.api;

import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.security.web.authentication.UserConnectionSignUp;
import com.sendish.api.social.connect.JpaUsersConnectionRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.util.Assert;

@Configuration
public class SocialConfig {

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Configuration
    @EnableSocial
    protected static class CustomSocialConfig extends SocialConfigurerAdapter {
    	
    	@Bean
    	public ConnectionSignUp userConnectionSignUp() {
    		return new UserConnectionSignUp();
    	}

        @Override
        public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
            return new JpaUsersConnectionRepository(connectionFactoryLocator, Encryptors.noOpText(), userConnectionSignUp());
        }

        @Override
        public UserIdSource getUserIdSource() {
            return new SecurityContextUserIdSource();
        }
    }

    private static class SecurityContextUserIdSource implements UserIdSource {

        @Override
        public String getUserId() {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            Assert.state(authentication != null, "Unable to get a " + "ConnectionRepository: no user signed in");
            AuthUser authUser = (AuthUser) authentication.getPrincipal();

            return authUser.getUserId().toString();
        }

    }

}
