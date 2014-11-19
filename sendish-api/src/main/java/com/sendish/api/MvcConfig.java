package com.sendish.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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

}
