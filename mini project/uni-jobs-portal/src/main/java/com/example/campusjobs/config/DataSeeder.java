package com.example.campusjobs.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.campusjobs.model.Job;
import com.example.campusjobs.repo.JobRepository;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedJobs(JobRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Job(
                    "Staff งานปฐมนิเทศ",
                    "ช่วยต้อนรับน้องใหม่ ณ หอประชุม",
                    "admin",
                    "บริการ, ยิ้มแย้ม, ทำงานทีม",
                    LocalDate.of(2025, 11, 1),
                    LocalDate.of(2025, 11, 15),
                    null
                ));

                repo.save(new Job(
                    "จิตอาสา Big Cleaning Day",
                    "ร่วมทำความสะอาดอาคารเรียนและบริเวณรอบมหาวิทยาลัย",
                    "admin",
                    "ขยัน, รับผิดชอบ, ทำงานทีม",
                    LocalDate.of(2025, 11, 5),
                    LocalDate.of(2025, 11, 20),
                    null
                ));
            }
        };
    }
}

