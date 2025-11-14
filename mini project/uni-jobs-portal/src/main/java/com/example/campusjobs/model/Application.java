package com.example.campusjobs.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Transient;
import java.util.Map;

@Entity
@Table(name = "applications")
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    private String applicantUsername; // student username (email)

    @Column(columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String nickname;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String yearLevel; 

    @Column(columnDefinition = "NVARCHAR(255)")
    private String faculty;

    @Column(columnDefinition = "NVARCHAR(2000)")
    private String bio;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String studentId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String department; // ฝ่ายที่สมัคร

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String answersJson;


    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private Instant appliedAt = Instant.now();

    public Application(){}

    public Application(Job job,
                       String applicantUsername,
                       String fullName,
                       String nickname,
                       String yearLevel,
                       String faculty,
                       String bio,
                       String studentId,
                       String email,
                       String phone,
                       String department,
                       String answersJson) {this.job = job;
                                            this.applicantUsername = applicantUsername;
                                            this.fullName = fullName;
                                            this.nickname = nickname;
                                            this.yearLevel = yearLevel;
                                            this.faculty = faculty;
                                            this.bio = bio;
                                            this.studentId = studentId;
                                            this.email = email;
                                            this.phone = phone;
                                            this.department = department;
                                            this.answersJson = answersJson;}

    public Long getId(){ return id; }
    public Job getJob(){ return job; }
    public String getApplicantUsername(){ return applicantUsername; }
    public String getFullName(){ return fullName; }
    public String getNickname(){ return nickname; }
    public String getYearLevel(){ return yearLevel; }
    public String getFaculty(){ return faculty; }
    public String getBio(){ return bio; }
    public String getStudentId(){ return studentId; }
    public String getEmail(){ return email; }
    public String getPhone(){ return phone; }
    public String getDepartment() { return department; }
    public String getAnswersJson() { return answersJson; }
    public ApplicationStatus getStatus(){ return status; }
    public Instant getAppliedAt(){ return appliedAt; }

    public void setId(Long id){ this.id = id; }
    public void setJob(Job job){ this.job = job; }
    public void setApplicantUsername(String u){ this.applicantUsername = u; }
    public void setFullName(String n){ this.fullName = n; }
    public void setNickname(String n){ this.nickname = n; }
    public void setYearLevel(String y){ this.yearLevel = y; }
    public void setFaculty(String f){ this.faculty = f; }
    public void setBio(String b){ this.bio = b; }
    public void setStudentId(String s){ this.studentId = s; }
    public void setEmail(String e){ this.email = e; }
    public void setPhone(String p){ this.phone = p; }
    public void setDepartment(String department) { this.department = department; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public void setStatus(ApplicationStatus s){ this.status = s; }
    public void setAppliedAt(Instant t){ this.appliedAt = t; }

    // แปลงคำตอบจาก JSON เป็น Map<String, String>
    @Transient
    public Map<String, String> getAnswersMap() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.answersJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
