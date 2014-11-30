package com.sendish.api;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.sendish.api.config.EmailSettings;

@Configuration
public class EmailConfiguration {
	
	@Autowired
	private EmailSettings settings;
	
	@Bean
	public JavaMailSender getJavaMailSenderImpl() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost(settings.getHost());
		javaMailSender.setPort(settings.getPort());
		javaMailSender.setUsername(settings.getUsername());
		javaMailSender.setPassword(settings.getPassword());

		Properties props = new Properties();

		javaMailSender.setJavaMailProperties(props);

		return javaMailSender;
	}

	@Bean
	public ClassLoaderTemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
		emailTemplateResolver.setPrefix("mails/");
		emailTemplateResolver.setSuffix(".html");
		emailTemplateResolver.setTemplateMode("HTML5");
		emailTemplateResolver.setCharacterEncoding("UTF-8");
		emailTemplateResolver.setOrder(1);

		return emailTemplateResolver;
	}
	
}
