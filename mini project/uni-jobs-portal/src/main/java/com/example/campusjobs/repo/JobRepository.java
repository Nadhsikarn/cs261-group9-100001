package com.example.campusjobs.repo;

import com.example.campusjobs.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByCreatorUsernameOrderByCreatedAtDesc(String creatorUsername);
    int countByCreatorUsername(String creatorUsername);

int countByCreatorUsernameAndCloseDateAfter(String creatorUsername, LocalDate today);
    
}
