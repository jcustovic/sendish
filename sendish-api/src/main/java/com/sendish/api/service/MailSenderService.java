package com.sendish.api.service;

import java.util.Map;

import javax.mail.MessagingException;

public interface MailSenderService {
	
	void sendEmail(String toEmail, String fromEmail, Map<String, Object> variables, String tplName) throws MessagingException;

}
