package com.sendish.api.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.dto.UserRegistration;
import com.sendish.api.service.MailSenderService;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;

import org.springframework.util.StreamUtils;

@Service
@Transactional
public class RegistrationServiceImpl {

    @Autowired
    private UserServiceImpl userService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MailSenderService mailSenderService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private NewUserAutomaticPhotoAndInboxSenderImpl newUserAutomaticPhotoAndInboxSender;
	
	@Value("${app.registration.mail.from}")
	private String fromEmail;
	
	public void registerNewUser(UserRegistration userRegistration) {
        User user = userService.createUser(userRegistration.getEmail(), userRegistration.getEmail(), userRegistration.getPassword(), userRegistration.getNickname(), true);
		Map<String, Object> variables = new HashMap<>();
		variables.put("user", user);
		
		Map<String, byte[]> inlineImages = getInlineImages();
		
		try {
			mailSenderService.sendEmail(user.getEmail(), fromEmail, "Welcome to Sendish", variables, "verify-registration", inlineImages);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

    private Map<String, byte[]> getInlineImages() {
    	Map<String, byte[]> images = new HashMap<>();
    	images.put("logoImage", getResourceAsBytes("classpath:web/static/images/sendish_mustache_white_150x78.png"));
    	
		return images;
	}

	private byte[] getResourceAsBytes(String resourceString) {
		try {
			InputStream resource = context.getResource(resourceString).getInputStream();
			
			return StreamUtils.copyToByteArray(resource);
		} catch (IOException e) {
			return null;
		}
	}

	public boolean verifyRegistrationToken(String token, String username) {
        final User user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null || Boolean.TRUE.equals(user.getEmailConfirmed())) {
            return false;
        } else if (token != null && token.equals(user.getVerificationCode())) {
            user.setEmailConfirmed(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            newUserAutomaticPhotoAndInboxSender.send(user);

            return true;
        }

        return false;
    }

	public boolean verifyChangePasswordToken(String token, String username) {
		final User user = userRepository.findByUsernameIgnoreCase(username);
		if (user == null) {
			return false;
		}
		
		return token.equals(user.getVerificationCode());
	}

}
