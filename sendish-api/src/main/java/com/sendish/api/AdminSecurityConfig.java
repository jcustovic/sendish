package com.sendish.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String REALM_NAME = "Sendish Admin Realm";
    public static final String ROLE_ADMIN = "ADMIN";

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .requestMatchers().antMatchers("/api/admin/**").and()
            .csrf().disable()
            .headers().frameOptions().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .anyRequest().hasRole(ROLE_ADMIN);

        http
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
            .and()
                .addFilter(digestAuthenticationFilter(digestEntryPoint()));
        }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("pimpek").password("tijemali").roles(ROLE_ADMIN);
    }

	public DigestAuthenticationFilter digestAuthenticationFilter(DigestAuthenticationEntryPoint digestAuthenticationEntryPoint) throws Exception {
		DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
		digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
		digestAuthenticationFilter.setUserDetailsService(userDetailsServiceBean());

		return digestAuthenticationFilter;
	}

    public DelegatingAuthenticationEntryPoint authenticationEntryPoint() {
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(AnyRequestMatcher.INSTANCE, digestEntryPoint());

        DelegatingAuthenticationEntryPoint authEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
        authEntryPoint.setDefaultEntryPoint(digestEntryPoint());

        return authEntryPoint;
    }

	@Bean
	public DigestAuthenticationEntryPoint digestEntryPoint() {
		DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
		digestAuthenticationEntryPoint.setKey("akdas/(&%$");
		digestAuthenticationEntryPoint.setRealmName(REALM_NAME);

		return digestAuthenticationEntryPoint;
	}

}
