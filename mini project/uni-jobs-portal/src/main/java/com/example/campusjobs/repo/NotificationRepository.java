package com.example.campusjobs.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.campusjobs.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}