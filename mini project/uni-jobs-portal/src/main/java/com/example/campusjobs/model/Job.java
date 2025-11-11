package com.example.campusjobs.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id งาน

    @Column(columnDefinition = "NVARCHAR(255)")
    private String creatorUsername; // อีเมลผู้สร้าง (อาจารย์)

    @Column(columnDefinition = "NVARCHAR(255)")
    private String title; // ชื่องาน

    @Column(length = 4000, columnDefinition = "NVARCHAR(255)")
    private String description; // รายละเอียดงาน

    @Column(length = 2000, columnDefinition = "NVARCHAR(255)")
    private String requiredSkill; // ทักษะที่ต้องการ

    private LocalDate openDate; // วันที่เปิดรับสมัคร
    private LocalDate closeDate; // วันที่ปิดรับสมัคร

    private String imagePath; // รูปที่อัพโหลด

    private Instant createdAt = Instant.now(); // เวลาที่สร้างงาน

    public Job() {}

    public Job(String title, String description, String creatorUsername, String requiredSkill,
               LocalDate openDate, LocalDate closeDate, String imagePath) {
        this.title = title;
        this.description = description;
        this.creatorUsername = creatorUsername;
        this.requiredSkill = requiredSkill;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.imagePath = imagePath;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatorUsername() { return creatorUsername; }
    public String getRequiredSkill() { return requiredSkill; }
    public LocalDate getOpenDate() { return openDate; }
    public LocalDate getCloseDate() { return closeDate; }
    public String getImagePath() { return imagePath; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String t) { this.title = t; }
    public void setDescription(String d) { this.description = d; }
    public void setCreatorUsername(String u) { this.creatorUsername = u; }
    public void setRequiredSkill(String q) { this.requiredSkill = q; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCreatedAt(Instant i) { this.createdAt = i; }
}
