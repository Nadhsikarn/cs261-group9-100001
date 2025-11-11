package com.example.campusjobs.controller;

import com.example.campusjobs.model.*;
import com.example.campusjobs.repo.ApplicationRepository;
import com.example.campusjobs.repo.JobRepository;
import com.example.campusjobs.util.SecUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors; 

@Controller
@Validated
@RequestMapping("/teacher")
public class TeacherJobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public TeacherJobController(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/jobs")
    public String myJobs(Model model) {
        String me = SecUtil.currentUsername();
        model.addAttribute("jobs", jobRepository.findByCreatorUsernameOrderByCreatedAtDesc(me));
        return "teacher_jobs";
    }

    @GetMapping("/jobs/new")
    public String newJobForm() { 
        return "teacher_job_new"; 
    }

    @PostMapping("/jobs")
    public String createJob(@RequestParam("title") @NotBlank String title,
                            @RequestParam("description") @NotBlank String description,
                            @RequestParam("requiredSkill") @NotBlank String requiredSkill,
                            @RequestParam("openDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openDate,
                            @RequestParam("closeDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDate,
                            @RequestParam("image") MultipartFile image,
                            RedirectAttributes ra) {
        String me = SecUtil.currentUsername();

    String imagePath = null;
    try {
        if (image != null && !image.isEmpty()) {
            // เปลี่ยนชื่อไฟล์เพื่อป้องกันการซ้ำกัน
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path uploadDir = Paths.get("./jobUploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            image.transferTo(filePath);
            imagePath = "/jobUploads/" + fileName;
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

        Job job = new Job(title, description, me, requiredSkill, openDate, closeDate, imagePath);
        jobRepository.save(job);

        ra.addFlashAttribute("popupMsg", "Your post has been published !");
        return "redirect:/teacher/jobs";
    }

    // ✅ แก้เฉพาะตรงนี้เท่านั้น
    @GetMapping("/jobs/{id}/applications")
    public String viewApplications(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getCreatorUsername().equals(SecUtil.currentUsername())) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์เข้าถึงงานนี้");
            return "redirect:/teacher/jobs";
        }

        var job = jobOpt.get();
        var apps = applicationRepository.findByJobIdOrderByAppliedAtDesc(id);

        var approvedApplicants = apps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPROVED)
                .collect(Collectors.toList());

        model.addAttribute("job", job);
        model.addAttribute("apps", apps);
        model.addAttribute("approvedApplicants", approvedApplicants);

        return "teacher_applications";
    }

    @PostMapping("/applications/{appId}/approve")
    public String approve(@PathVariable("appId") Long appId, RedirectAttributes ra) {
        return updateStatus(appId, ApplicationStatus.APPROVED, ra);
    }

    @PostMapping("/applications/{appId}/reject")
    public String reject(@PathVariable("appId") Long appId, RedirectAttributes ra) {
        return updateStatus(appId, ApplicationStatus.REJECTED, ra);
    }

    private String updateStatus(Long appId, ApplicationStatus status, RedirectAttributes ra) {
        var appOpt = applicationRepository.findById(appId);
        if (appOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบใบสมัคร");
            return "redirect:/teacher/jobs";
        }

        var app = appOpt.get();
        var job = app.getJob();
        if (!job.getCreatorUsername().equals(SecUtil.currentUsername())) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์ปรับสถานะงานนี้");
            return "redirect:/teacher/jobs";
        }

        app.setStatus(status);
        applicationRepository.save(app);
        ra.addFlashAttribute("msg", "อัปเดตสถานะเรียบร้อย");
        return "redirect:/teacher/jobs/" + job.getId() + "/applications";
    }
    //อันนี้เพิ่มมาให้กด link ได้เฉยๆ ยังไม่ได้ใส่ logic ใดๆ
    @GetMapping("/applicant")
    public String applicantsPage() {
        return "teacher_applicant";
    }

    @GetMapping("/interview")
    public String interviewsPage() {
        return "teacher_interview";
    }

    @GetMapping("/final")
    public String finalPage() {
        return "teacher_final";
    }
}
