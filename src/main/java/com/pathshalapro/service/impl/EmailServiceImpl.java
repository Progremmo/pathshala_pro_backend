package com.pathshalapro.service.impl;

import com.pathshalapro.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
// import jakarta.mail.internet.MimeMessage;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EmailServiceImpl implements EmailService {

    // private final JavaMailSender mailSender;

    @Value("${spring.mail.username:admin@pathshalapro.com}")
    private String fromEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @Override
    public void sendEmail(String to, String subject, String text) {
        try {
            /*
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            */
            sendBrevoEmail(to, subject, text, false);
            log.info("Email sent successfully to {} via Brevo", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            /*
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            */
            sendBrevoEmail(to, subject, htmlContent, true);
            log.info("HTML Email sent successfully to {} via Brevo", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}", to, e);
        }
    }

    private void sendBrevoEmail(String to, String subject, String content, boolean isHtml) {
        String url = "https://api.brevo.com/v3/smtp/email";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        
        Map<String, String> sender = new HashMap<>();
        sender.put("email", fromEmail);
        sender.put("name", "Pathshala Pro");
        body.put("sender", sender);
        
        Map<String, String> toRecipient = new HashMap<>();
        toRecipient.put("email", to);
        body.put("to", List.of(toRecipient));
        
        body.put("subject", subject);
        if (isHtml) {
            body.put("htmlContent", content);
        } else {
            body.put("textContent", content);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to send email to {} via Brevo. Status: {}, Body: {}", to, response.getStatusCode(), response.getBody());
        }
    }
}
