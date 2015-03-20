package com.sendish.api.service.impl;

import com.sendish.api.service.MailSenderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.util.Map;

@Service
public class ThymleafMailSenderServiceImpl implements MailSenderService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Value("${app.baseUrl}")
	private String baseUrl;

	@Override
	public void sendEmail(String toEmail, String fromEmail, String subject, Map<String, Object> variables, String tplName, Map<String, byte[]> pngImages) throws MessagingException {
		final Context ctx = new Context();
		ctx.setVariables(variables);
		ctx.setVariable("baseUrl", baseUrl);

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
		message.setSubject(subject);
		message.setFrom(fromEmail);
		message.setTo(toEmail);

		// Create the HTML body using Thymeleaf
		final String htmlContent = templateEngine.process(tplName, ctx);
		message.setText(htmlContent, true); // true = isHtml

        if (pngImages != null) {
            for (Map.Entry<String, byte[]> imageEntry : pngImages.entrySet()) {
                // Add the inline image, referenced from the HTML code as
                // "cid:${imageResourceName}"
                final InputStreamSource imageSource = new ByteArrayResource(imageEntry.getValue());
                message.addInline(imageEntry.getKey(), imageSource, MediaType.IMAGE_PNG_VALUE);
            }
        }

		// Send mail
		mailSender.send(mimeMessage);
	}

}
