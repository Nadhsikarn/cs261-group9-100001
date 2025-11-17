package com.example.campusjobs.repo;

import com.example.campusjobs.model.JobImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface JobImageRepository extends JpaRepository<JobImage, Long> {
    @Transactional
    @Modifying
    void deleteByJobId(Long jobId);
}
