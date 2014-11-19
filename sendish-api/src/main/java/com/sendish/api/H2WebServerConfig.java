package com.sendish.api;

import org.h2.server.web.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

@Configuration
@ConditionalOnClass({ WebServlet.class })
public class H2WebServerConfig implements ServletContextInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2WebServerConfig.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOGGER.info("H2 WebServlet initialization start");
        ServletRegistration.Dynamic dynamic = servletContext.addServlet("h2", new WebServlet());
        dynamic.addMapping("/h2/*");
        LOGGER.info("H2 WebServlet initialization finished");
    }

}