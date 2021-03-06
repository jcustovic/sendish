package com.sendish.api;

import java.util.List;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.sendish.api.web.interceptor.DeviceAndVersionDetectorInterceptor;
import com.sendish.api.web.method.support.AuthUserArgumentResolver;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/documentation/login").setViewName("documentation/login");

        registry.addViewController("/swagger-ui/doc").setViewName("swagger");
        registry.addViewController("/swagger-ui").setViewName("redirect:/swagger-ui/doc");
        registry.addViewController("/documentation").setViewName("redirect:/swagger-ui/doc");
    }
    
    @Bean
    public DeviceAndVersionDetectorInterceptor deviceAndVersionDetectorInterceptor() {
    	return new DeviceAndVersionDetectorInterceptor();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(deviceAndVersionDetectorInterceptor());
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("5MB");
        factory.setMaxRequestSize("5MB");

        return factory.createMultipartConfig();
    }

    @Bean
    AuthUserArgumentResolver authUserArgumentResolver() {
        return new AuthUserArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(authUserArgumentResolver());
    }

}
