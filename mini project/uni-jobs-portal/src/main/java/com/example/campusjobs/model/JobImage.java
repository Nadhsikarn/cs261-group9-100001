package com.example.campusjobs.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_images")
public class JobImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String imagePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    public JobImage() {}

    public JobImage(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getId() { return id; }
    public String getImagePath() { return imagePath; }
    public Job getJob() { return job; }

    public void setId(Long id) { this.id = id; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setJob(Job job) { this.job = job; }
}