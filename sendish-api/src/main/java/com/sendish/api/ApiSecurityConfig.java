package com.sendish.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;

import com.sendish.api.security.authentication.CustomUserDetailsService;
import com.sendish.api.security.web.authentication.SocialAuthenticationFilter;
import com.sendish.repository.UserSocialConnectionRepository;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
@EnableWebSecurity
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String REALM_NAME = "Sendish API Realm";
    
    @Autowired
    private UsersConnectionRepository usersConnectionRepository;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private UserSocialConnectionRepository userSocialConnectionRepository;
    
    @Autowired
    private ShaPasswordEncoder shaPasswordEncoder;

    @Bean
    public CustomUserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName(REALM_NAME);

        return basicAuthenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SocialAuthenticationFilter socialAuthenticationFilter = new SocialAuthenticationFilter(basicAuthenticationEntryPoint());
        socialAuthenticationFilter.setUserDetailsService(userDetailsService());
        socialAuthenticationFilter.setUsersConnectionRepository(usersConnectionRepository);
        socialAuthenticationFilter.setConnectionFactoryLocator(connectionFactoryLocator);
        socialAuthenticationFilter.setUserSocialConnectionRepository(userSocialConnectionRepository);

        http
            .csrf().disable()
            .headers().frameOptions().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .addFilterBefore(socialAuthenticationFilter, BasicAuthenticationFilter.class)
            .httpBasic().realmName(REALM_NAME).and()
            .authorizeRequests()
	            .antMatchers("/h2/**").permitAll()
	            .antMatchers("/").permitAll()
	            .antMatchers("/api/v1.0/registration/**").permitAll()
                .antMatchers("/api/d/registration/**").permitAll()
	            .anyRequest().fullyAuthenticated();
        //.and().formLogin().loginPage("/login").failureUrl("/login?error").permitAll();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(shaPasswordEncoder);
    }

}
