package com.example.campusjobs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 2. อ่านค่า "username" มาจาก application.properties
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            // 3. ใช้ตัวแปรที่อ่านมา (ซึ่งจะตรงกับ .properties เสมอ)
            message.setFrom(senderEmail); 
            
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}