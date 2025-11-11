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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Job job;

    private String applicantUsername; // student username (email)

    @Column(columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String studentId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String phone;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private Instant appliedAt = Instant.now();

    public Application(){}

    public Application(Job job, String applicantUsername, String fullName, String studentId, String email, String phone){
        this.job = job;
        this.applicantUsername = applicantUsername;
        this.fullName = fullName;
        this.studentId = studentId;
        this.email = email;
        this.phone = phone;
    }

    public Long getId(){ return id; }
    public Job getJob(){ return job; }
    public String getApplicantUsername(){ return applicantUsername; }
    public String getFullName(){ return fullName; }
    public String getStudentId(){ return studentId; }
    public String getEmail(){ return email; }
    public String getPhone(){ return phone; }
    public ApplicationStatus getStatus(){ return status; }
    public Instant getAppliedAt(){ return appliedAt; }

    public void setId(Long id){ this.id = id; }
    public void setJob(Job job){ this.job = job; }
    public void setApplicantUsername(String u){ this.applicantUsername = u; }
    public void setFullName(String n){ this.fullName = n; }
    public void setStudentId(String s){ this.studentId = s; }
    public void setEmail(String e){ this.email = e; }
    public void setPhone(String p){ this.phone = p; }
    public void setStatus(ApplicationStatus s){ this.status = s; }
    public void setAppliedAt(Instant t){ this.appliedAt = t; }
}
