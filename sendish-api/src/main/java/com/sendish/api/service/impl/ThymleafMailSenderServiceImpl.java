package com.sendish.api.service.impl;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.sendish.api.service.MailSenderService;

@Service
public class ThymleafMailSenderServiceImpl implements MailSenderService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine templateEngine;

	@Override
	public void sendEmail(String toEmail, String fromEmail, Map<String, Object> variables, String tplName) throws MessagingException {
		final Context ctx = new Context();
		ctx.setVariables(variables);

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
		message.setSubject("Example HTML email with inline image");
		message.setFrom(fromEmail);
		message.setTo(toEmail);

		// Create the HTML body using Thymeleaf
		final String htmlContent = templateEngine.process(tplName, ctx);
		message.setText(htmlContent, true); // true = isHtml

		// Add the inline image, referenced from the HTML code as
		// "cid:${imageResourceName}"
		// final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
		// message.addInline(imageResourceName, imageSource, imageContentType);

		// Send mail
		mailSender.send(mimeMessage);
	}

}
