package com.example.campusjobs.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private Instant createdAt = Instant.now(); // เวลาที่สร้างงาน
    
    // รูปภาพที่เกี่ยวข้องกับงานนี้
    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private JobImage image;

    // คำถามเพิ่มเติมสำหรับงานนี้
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    // ฝ่ายเพิ่มเติมที่เกี่ยวข้องกับงานนี้
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Department> departments = new ArrayList<>();

    // รายการสมัครงานที่เกี่ยวข้องกับงานนี้
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications = new ArrayList<>();

    public JobImage getImage() { return image; }

    public Job() {}

    public Job(String title, String description, String creatorUsername, String requiredSkill,
               LocalDate openDate, LocalDate closeDate) {
        this.title = title;
        this.description = description;
        this.creatorUsername = creatorUsername;
        this.requiredSkill = requiredSkill;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }

    
    // เพิ่มรูปภาพใหม่ให้กับงานนี้
    public void setImage(JobImage image) {
        this.image = image;
        if (image != null) {
             image.setJob(this);
        }
    }

    // เพิ่มคำถามใหม่ให้กับงานนี้
    public void addQuestion(Question question) {
    questions.add(question);
    question.setJob(this);
    }

    // เพิ่มฝ่ายใหม่ให้กับงานนี้
    public void addDepartment(Department department) {
    departments.add(department);
    department.setJob(this);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatorUsername() { return creatorUsername; }
    public String getRequiredSkill() { return requiredSkill; }
    public LocalDate getOpenDate() { return openDate; }
    public LocalDate getCloseDate() { return closeDate; }
    public List<Question> getQuestions() { return questions; }
    public List<Department> getDepartments() { return departments; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String t) { this.title = t; }
    public void setDescription(String d) { this.description = d; }
    public void setCreatorUsername(String u) { this.creatorUsername = u; }
    public void setRequiredSkill(String q) { this.requiredSkill = q; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public void setQuestions(List<Question> questions) { this.questions = questions;}
    public void setDepartments(List<Department> departments) { this.departments = departments; }
    public void setCreatedAt(Instant i) { this.createdAt = i; }
}
