package com.sendish.api.service;

import java.util.Map;

import javax.mail.MessagingException;

public interface MailSenderService {

	void sendEmail(String toEmail, String fromEmail, String subject,
			Map<String, Object> variables, String tplName,
			Map<String, byte[]> pngImages) throws MessagingException;

}
