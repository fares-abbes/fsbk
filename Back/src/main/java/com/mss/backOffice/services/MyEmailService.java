package com.mss.backOffice.services;


import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MyEmailService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${spring.mail.username}")
	private String email;

	@Value("${spring.mail.password}")
	private String password;
	@Autowired
	private JavaMailSender javaMailSender;

	public void sendOtpMessage(String to, String subject, String message) throws MessagingException, UnsupportedEncodingException {
		MimeMessage messag = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(messag);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(message);
		helper.setFrom(email, "MSS MONETIC");

		//logger.info(subject);
		//logger.info(to);
		//logger.info(message);
		javaMailSender.send(messag);
 

	}
	
	public void sendOtpMessage(String to, String subject, String message,String[] cc) throws MessagingException, UnsupportedEncodingException {
		MimeMessage messag = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(messag, true, "UTF-8");
		helper.setTo(to.trim());
		if(cc != null && cc.length != 0){
			helper.setCc(cc);
		}
		helper.setSubject(subject);
		helper.setText(message, true);
		helper.setFrom(email, "MSS MONETIC");
		javaMailSender.send(messag);
	}

}
