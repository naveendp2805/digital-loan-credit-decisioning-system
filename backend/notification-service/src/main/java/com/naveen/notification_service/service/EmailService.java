package com.naveen.notification_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(String recipient, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(recipient);
        mail.setSubject(subject);
        mail.setText(message);

        javaMailSender.send(mail);
    }
}
