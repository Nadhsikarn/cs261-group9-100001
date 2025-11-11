package com.example.campusjobs.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    public Question() {}
    public Question(String text) {
        this.text = text;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Job getJob() { return job; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setJob(Job job) { this.job = job; }
}