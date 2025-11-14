package com.example.campusjobs.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.campusjobs.model.ApplicationStatus;
import com.example.campusjobs.model.Department;
import com.example.campusjobs.model.Job;
import com.example.campusjobs.model.Notification;
import com.example.campusjobs.model.Question;
import com.example.campusjobs.model.User;
import com.example.campusjobs.repo.ApplicationRepository;
import com.example.campusjobs.repo.JobRepository;
import com.example.campusjobs.repo.NotificationRepository;
import com.example.campusjobs.repo.QuestionRepository;
import com.example.campusjobs.repo.UserRepository;
import com.example.campusjobs.service.EmailService;
import com.example.campusjobs.util.SecUtil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Controller
@Validated
@RequestMapping("/teacher")
public class TeacherJobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final QuestionRepository questionRepository;
    private final EmailService emailService;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;

    public TeacherJobController(JobRepository jobRepository, ApplicationRepository applicationRepository,  QuestionRepository questionRepository,
                                NotificationRepository notificationRepository, UserRepository userRepository, EmailService emailService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.emailService = emailService;
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
                            @RequestParam(value = "departments", required = false) List<String> departments,
                            @RequestParam("image") MultipartFile image,
                            @RequestParam(value = "questions", required = false) List<String> questions, 
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

    // บันทึกคำถามเพิ่มเติมถ้ามี
    if (questions != null) {
        for (String text : questions) {
            if (text != null && !text.isBlank()) {
                Question q = new Question(text);
                q.setJob(job);
                questionRepository.save(q);
            }
        }
    }

    // บันทึกฝ่ายเพิ่มเติมถ้ามี
    if (departments != null && !departments.isEmpty()) {
        for (String deptName : departments) {
            if (!deptName.trim().isEmpty()) {
                job.addDepartment(new Department(deptName.trim()));
            }
        }
        jobRepository.save(job);
    }
    try {
        // 1. ค้นหานักเรียนทั้งหมด (เราใช้ Role "STUDENT" ตามที่คุณตั้งไว้)
        List<User> allStudents = userRepository.findByRole("ROLE_STUDENT");

        // 2. สร้าง List ว่างๆ เพื่อเก็บ Noti ทั้งหมด (เพื่อประสิทธิภาพ)
        List<Notification> notificationsToSave = new ArrayList<>();

        // 3. วนลูปนักเรียนทุกคนเพื่อสร้าง Noti
        for (User student : allStudents) {
            Notification notification = new Notification();
            notification.setUserId(student.getId()); // ⬅️ ส่ง Noti ไปหานักเรียนคนนี้
            notification.setDescription("เปิดรับสมัคร: Staff '" + job.getTitle() + "' Please check the new details");
            
            // (ผมเดาว่านี่คือ URL ที่นักเรียนใช้ดูรายละเอียดงาน)
            notification.setLinkUrl("/jobs/preview?id=" + job.getId()); 
            
            notificationsToSave.add(notification);
        }

        // 4. บันทึก Noti ทั้งหมดลง DB ในครั้งเดียว (เร็วและดีกว่า save ทีละอัน)
        if (!notificationsToSave.isEmpty()) {
            notificationRepository.saveAll(notificationsToSave);
        }

    } catch (Exception e) {
        e.printStackTrace(); // (พิมพ์ Error ไว้ดูใน Log)
        
        // (Optional) แจ้งเตือน Teacher เบาๆ ว่าส่ง Noti ไม่สำเร็จ
        ra.addFlashAttribute("err", "เผยแพร่โพสต์สำเร็จ แต่ส่ง Notification หานักเรียนล้มเหลว!");
    }

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

    @PostMapping("/applications/{appId}/interview")
    public String interview(@PathVariable("appId") Long appId, RedirectAttributes ra) {
        return updateStatus(appId, ApplicationStatus.INTERVIEW, ra);
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
        try {
            // 4.1 ดึง User มาเตรียมไว้ (ทำแค่ครั้งเดียว)
            String applicantUsername = app.getApplicantUsername();
            User userToNotify = userRepository.findByUsername(applicantUsername)
                    .orElseThrow(() -> new Exception("ไม่พบผู้ใช้งาน" + applicantUsername)); // ดีกว่า check isEmpty
            Long applicantId = userToNotify.getId();
            String userEmail = app.getEmail();

            Notification notification = new Notification();
            notification.setUserId(applicantId);
            notification.setLinkUrl("/student/applications");

            String notiDescription = "";
            String emailDescription = "";
            String subject = "";
            switch (status) {
                case APPROVED:
                    subject = "ยินดีด้วย! คุณผ่านการคัดเลือกเป็น Staff ในงาน " + job.getTitle();
                    notiDescription = "Congratulations! You’ve been selected for Staff " + job.getTitle() + " More details will be sent to your email.";
                    emailDescription = "ขอแสดงความยินดีกับคุณ "+ app.getFullName() +" ที่ผ่านการสัมภาษณ์และได้รับเลือกเป็น Staff ของงาน " + 
                                        job.getTitle() + " โปรดรอรับรายละเอียดการติดต่อจากทีมงานอีกครั้งทาง Email ที่แจ้งไว้";
                    break;
                case INTERVIEW:
                    subject = "คุณได้รับสิทธิ์สัมภาษณ์งานเพื่อคัดเลือกเป็น Staff ในงาน " + job.getTitle();
                    notiDescription = "Congratulations! You’re shortlisted for Staff " + job.getTitle() + " check your email for interview details.";
                    emailDescription = "ขอแสดงความยินดีกับคุณ "+ app.getFullName() +" ที่ผ่านการคัดเลือกรอบที่ 1 ได้รับสิทธิ์สัมภาษณ์การเป็น Staff ในงาน " + 
                                        job.getTitle() + " โปรดรอรับรายละเอียดการสัมภาษณ์จากทีมงานทาง Email ที่แจ้งไว้";
                    break;
                case REJECTED:
                    subject = "ขอแสดงความเสียใจด้วย คุณไม่ผ่านการคัดเลือก " + job.getTitle();
                    notiDescription = "Unfortunately, you did not pass the selection process to become a staff member. " + job.getTitle();
                    emailDescription = "ขอแสดงความเสียใจกับคุณ " + app.getFullName() + " เป็นอย่างยิ่ง " + 
                                        "คุณยังไม่ผ่านการคัดเลือกจากทางทีมงานสำหรับตำแหน่ง Staff งาน " + job.getTitle() + " ในครั้งนี้ " +
                                        "เราขอขอบคุณเป็นอย่างสูงสำหรับความสนใจ และหวังเป็นอย่างยิ่งว่าจะมีโอกาสได้ร่วมงานกับคุณในอนาคต";
                    break;
                default:
                    // ถ้าเป็นสถานะอื่น (เช่น PENDING) ก็ไม่ต้องทำอะไร
                    ra.addFlashAttribute("msg", "อัปเดตสถานะเรียบร้อย (ไม่มี Notification)");
                    return "redirect:/teacher/jobs/" + job.getId() + "/applications";
            }
            notification.setDescription(notiDescription);
            notificationRepository.save(notification);
            if (userEmail != null && !userEmail.isBlank()) { // (เช็กก่อนว่า User มีอีเมล)
                emailService.sendHtmlMessage(userEmail, subject, emailDescription);
            }
        }
        catch (Exception e) {
            // จัดการ Error (ทำแค่ครั้งเดียว)
            ra.addFlashAttribute("err", "อัปเดตสถานะสำเร็จ แต่ส่ง Notification ล้มเหลว: " + e.getMessage());
        }
        ra.addFlashAttribute("msg", "อัปเดตสถานะเรียบร้อย");
        return "redirect:/teacher/jobs/" + job.getId() + "/applications";
    }
    //อันนี้เพิ่มมาให้กด link ได้เฉยๆ ยังไม่ได้ใส่ logic ใดๆ
   @GetMapping("/applicant")
    public String applicantsPage(Model model) {
        String me = SecUtil.currentUsername();
        var jobs = jobRepository.findByCreatorUsernameOrderByCreatedAtDesc(me);
        model.addAttribute("jobs", jobs);
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