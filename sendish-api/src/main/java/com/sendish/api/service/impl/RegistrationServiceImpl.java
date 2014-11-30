package com.sendish.api.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sendish.api.dto.UserRegistration;
import com.sendish.api.service.MailSenderService;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;

@Service
@Transactional
public class RegistrationServiceImpl {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private ShaPasswordEncoder shaPasswordEncoder;
	
	@Autowired
	private MailSenderService mailSenderService;
	
	@Value("${app.registration.mail.from}")
	private String fromEmail;
	
	public void registerNewUser(UserRegistration userRegistration) {
		User user = new User();
		user.setUsername(userRegistration.getEmail());
		user.setEmail(userRegistration.getEmail());
		user.setPassword(shaPasswordEncoder.encodePassword(userRegistration.getPassword(), null));
		user.setNickname(userRegistration.getNickname());
		
		String verificationCode = UUID.randomUUID().toString();
		user.setVerificationCode(verificationCode);
		
		userRepository.save(user);
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("user", user);
		
		try {
			mailSenderService.sendEmail(user.getEmail(), fromEmail, variables, "verify-registration");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}
