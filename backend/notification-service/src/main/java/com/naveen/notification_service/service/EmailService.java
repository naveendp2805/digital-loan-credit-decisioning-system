package com.naveen.notification_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public void sendEmail(String recipient, String subject, String message) {

        Exception lastException = null;

        for(int attempt=1; attempt <= MAX_ATTEMPTS; attempt++)
        {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();

                mail.setTo(recipient);
                mail.setSubject(subject);
                mail.setText(message);

                javaMailSender.send(mail);

                return;
            } catch(Exception e) {
                lastException = e;

                if(attempt < MAX_ATTEMPTS)
                {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch(InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Email retry interrupted", interruptedException);
                    }
                }
            }
        }

        throw new RuntimeException("Failed to send email after " + MAX_ATTEMPTS + " attempts", lastException);
    }
}
