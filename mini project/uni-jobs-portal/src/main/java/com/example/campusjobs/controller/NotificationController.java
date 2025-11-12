package com.example.campusjobs.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice; // (ตัว SecUtil ที่คุณใช้อยู่)
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.campusjobs.model.Notification;
import com.example.campusjobs.model.User;
import com.example.campusjobs.repo.NotificationRepository;
import com.example.campusjobs.repo.UserRepository;
import com.example.campusjobs.util.SecUtil;

@ControllerAdvice
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Method นี้จะทำงาน "ก่อน" Controller ทุกตัว
     * และจะเพิ่ม Attribute ชื่อ "notifications" ลงใน Model ให้อัตโนมัติ
     */
    @ModelAttribute("notifications")
    public List<Notification> getNotificationsForCurrentUser() {
        // 1. หา Username ที่ login อยู่
        String currentUsername = SecUtil.currentUsername();
        if (currentUsername == null || currentUsername.equals("anonymousUser")) {
            return Collections.emptyList(); // ถ้ายังไม่ login ก็ไม่ต้องแสดง
        }

        // 2. หา ID (Long) ของ User
        Optional<User> userOpt = userRepository.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return Collections.emptyList(); // ไม่เจอ User
        }
        Long currentUserId = userOpt.get().getId();

        // 3. ค้นหา Notifications ของ User คนนี้ (เรียงจากใหม่ไปเก่า)
        // ** คุณต้องไปเพิ่ม Method นี้ใน NotificationRepository (ดูข้อ 1.2) **
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
    }
}