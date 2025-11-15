package com.example.campusjobs.repo;

import com.example.campusjobs.model.Application;
import com.example.campusjobs.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);
    List<Application> findByJobIdAndStatusInOrderByAppliedAtDesc(Long jobId, List<ApplicationStatus> statuses);
    List<Application> findByJobIdAndStatusOrderByAppliedAtDesc(Long jobId, ApplicationStatus status);
    List<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status);
    List<Application> findByApplicantUsernameOrderByAppliedAtDesc(String applicantUsername);
    boolean existsByJobIdAndApplicantUsername(Long jobId, String applicantUsername);
    @Query("SELECT a FROM Application a WHERE a.job.id = ?1 AND a.department = ?2 AND a.status = ?3 ORDER BY a.fullName ASC")
    List<Application> findStaffForDepartment(Long jobId, String department, ApplicationStatus status);
}
