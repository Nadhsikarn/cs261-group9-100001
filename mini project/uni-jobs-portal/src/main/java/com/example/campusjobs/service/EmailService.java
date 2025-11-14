package com.example.campusjobs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // 2. อ่านค่า "username" มาจาก application.properties
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendHtmlMessage(String to, String subject, String description) {
        
        try {
            // 1. สร้าง MimeMessage (ตัวจริง)
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            
            // 2. ใช้ Helper ในการสร้าง (ตั้งค่า UTF-8)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // 3. สร้าง "Context" ของ Thymeleaf (เหมือน Model ใน Controller)
            Context context = new Context();
            context.setVariable("emailSubject", subject); // ⬅️ ส่ง "หัวข้อ" ไปให้ Template
            context.setVariable("emailDescription", description); // ⬅️ ส่ง "เนื้อหา" ไปให้ Template

            // 4. สั่ง Thymeleaf สร้าง HTML (จาก Template ที่เราสร้างใน Step 1)
            String htmlContent = templateEngine.process("notification-template", context);

            // 5. ตั้งค่าอีเมล (เหมือนเดิม)
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // 6. ⬇️ นี่คือจุดที่ต่าง ⬇️
            // บอกว่านี่คือ HTML (true) ไม่ใช่ Text ธรรมดา
            helper.setText(htmlContent, true); 

            // 7. สั่งส่ง!
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            // (ถ้าใช้ MimeMessage ต้อง catch MessagingException)
            System.err.println("Error sending HTML email: " + e.getMessage());
        }
    }
}