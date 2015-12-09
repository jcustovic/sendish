package com.sendish.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class SwaggerSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String ROLE_DOCS = "DOCS";
    public static final String ROLE_DOCS_ADMIN = "DOCS_ADMIN";

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
            .authorizeRequests()
                .antMatchers("/api-docs/admin/**").hasRole(ROLE_DOCS_ADMIN)
                .anyRequest().hasRole(ROLE_DOCS);

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
                .withUser("docs").password("cHusica").roles(ROLE_DOCS)
                .and()
                .withUser("dev").password("devBlaBla123").roles(ROLE_DOCS)
                .and()
                .withUser("admin").password("cHusica").roles(ROLE_DOCS, ROLE_DOCS_ADMIN);
    }

}
