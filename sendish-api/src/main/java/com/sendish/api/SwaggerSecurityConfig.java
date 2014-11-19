package com.sendish.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

@Configuration
@Order(99)
public class SwaggerSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String REALM_NAME = "Sendish Docs Realm";
    public static final String ROLE_DOCS = "DOCS";

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .requestMatchers().antMatchers("/documentation/**", "/swagger-ui/**", "/api-docs/**").and()
            .csrf().disable()
            .headers().frameOptions().disable()
            .authorizeRequests().anyRequest().hasRole(ROLE_DOCS);

        // Digest
        /*
        http
            .exceptionHandling()
                .authenticationEntryPoint(digestEntryPoint()).and()
            .addFilter(digestAuthenticationFilter(digestEntryPoint()));
        */
        http
            .formLogin()
                .loginPage("/documentation/login")
                .failureUrl("/documentation/login")
                .defaultSuccessUrl("/documentation", true)
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/documentation/logout")
                .logoutSuccessUrl("/documentation/login")
                .permitAll();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("docs").password("docs").roles(ROLE_DOCS);
    }
    
	public DigestAuthenticationFilter digestAuthenticationFilter(DigestAuthenticationEntryPoint digestAuthenticationEntryPoint) throws Exception {
		DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
		digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
		digestAuthenticationFilter.setUserDetailsService(userDetailsServiceBean());
		
		return digestAuthenticationFilter;
	}

	@Bean
	public DigestAuthenticationEntryPoint digestEntryPoint() {
		DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
		digestAuthenticationEntryPoint.setKey("akdas/(&%$");
		digestAuthenticationEntryPoint.setRealmName(REALM_NAME);
		
		return digestAuthenticationEntryPoint;
	}

}
