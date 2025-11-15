package com.example.campusjobs.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;   
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

import com.example.campusjobs.model.Application;
import com.example.campusjobs.model.ApplicationStatus;
import com.example.campusjobs.model.Department;
import com.example.campusjobs.model.Job;
import com.example.campusjobs.model.JobImage;
import com.example.campusjobs.model.Notification;
import com.example.campusjobs.model.Question;
import com.example.campusjobs.model.User;
import com.example.campusjobs.repo.ApplicationRepository;
import com.example.campusjobs.repo.JobRepository;
import com.example.campusjobs.repo.DepartmentRepository;
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
    @org.springframework.beans.factory.annotation.Autowired
    private DepartmentRepository departmentRepository;

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
        return "teacher_job_new";
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
                            @RequestParam(value = "departments", required = false) List<String> departments,
                            @RequestParam(value = "questions", required = false) List<String> questions, 
                            RedirectAttributes ra) {
    String me = SecUtil.currentUsername();

    Job job = new Job(title, description, me, requiredSkill, openDate, closeDate);
    jobRepository.save(job);

    // บันทึกรูปภาพถ้ามี
    if (image != null && !image.isEmpty()) {
        try {
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

            String uploadFolder = System.getProperty("user.dir") + "/jobUploads";

            Path uploadDir = Paths.get(uploadFolder);
            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(fileName);
            image.transferTo(target.toFile());

            JobImage jobImage = new JobImage("/jobUploads/" + fileName);
            job.setImage(jobImage);

        } catch (IOException e) {
            e.printStackTrace();
            ra.addFlashAttribute("err", "อัปโหลดรูปไม่สำเร็จ");
        }
    }


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
    
    jobRepository.save(job);

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

    @GetMapping("/applicants/{jobId}")
    public String applicants(@PathVariable Long jobId, 
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "department", required = false) String department,
                        Model model, RedirectAttributes ra) {

        var jobOpt = jobRepository.findById(jobId);

        if (jobOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบบันทึกงาน");
            return "redirect:/teacher/jobs";
        }
    
        var job = jobOpt.get();
    
        String me = SecUtil.currentUsername();
        if (me == null || !job.getCreatorUsername().equals(me)) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์เข้าดูผู้สมัครงานนี้");
            return "redirect:/teacher/jobs";
        }
    
        List<ApplicationStatus> statusFilter = List.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.REJECTED
        );

        List<Application> apps = applicationRepository
            .findByJobIdAndStatusInOrderByAppliedAtDesc(jobId, statusFilter);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase();
            apps = apps.stream()
                    .filter(a -> a.getFullName() != null &&
                                 a.getFullName().toLowerCase().contains(kw))
                    .toList();
       }


        if (department != null && !department.equals("ALL")) {
            apps = apps.stream()
                    .filter(a -> a.getDepartment() != null &&
                            a.getDepartment().equalsIgnoreCase(department))
                    .toList();
        }

        model.addAttribute("job", job);
        model.addAttribute("apps", apps);
        model.addAttribute("keyword", keyword);
        model.addAttribute("department", department);
        model.addAttribute("departments", job.getDepartments());

        return "teacher_applicants_list"; 
    }

    @GetMapping("/applicants/{jobId}/detail/{appId}")
    public String applicantDetail(@PathVariable Long jobId,
                                  @PathVariable Long appId,
                                  Model model,
                                  RedirectAttributes ra) {

        var jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบงานนี้");
            return "redirect:/teacher/jobs";
        }

        var appOpt = applicationRepository.findById(appId);
        if (appOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบใบสมัครนี้");
            return "redirect:/teacher/applicants/" + jobId;
        }

        var job = jobOpt.get();
        var app = appOpt.get();

        // ตรวจสิทธิ์
        if (!job.getCreatorUsername().equals(SecUtil.currentUsername())) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์ดูข้อมูลผู้สมัครคนนี้");
            return "redirect:/teacher/jobs";
        }

        model.addAttribute("job", job);
        model.addAttribute("app", app);

        return "teacher_applicant_detail";
}


    @PostMapping("/applications/{appId}/approve")
public String approve(
        @PathVariable("appId") Long appId,
        @RequestParam(value = "from", required = false) String from,
        RedirectAttributes ra) {

    return updateStatus(appId, ApplicationStatus.APPROVED, from, ra);
}

@PostMapping("/applications/{appId}/interview")
public String interview(
        @PathVariable("appId") Long appId,
        @RequestParam(value = "from", required = false) String from,
        RedirectAttributes ra) {

    return updateStatus(appId, ApplicationStatus.INTERVIEW, from, ra);
}

private String redirectByFrom(Long jobId, String from) {
    if (from == null) from = "";   // กัน null ที่นี่

    return switch (from) {
        case "application" -> "redirect:/teacher/jobs/" + jobId + "/applications";
        case "applicant" -> "redirect:/teacher/applicants/" + jobId;
        case "interview" -> "redirect:/teacher/interview/" + jobId;
        default -> "redirect:/teacher/jobs";
    };
}

@PostMapping("/applications/{appId}/reject")
public String reject(
        @PathVariable("appId") Long appId,
        @RequestParam(value = "from", required = false) String from,
        RedirectAttributes ra) {

    return updateStatus(appId, ApplicationStatus.REJECTED, from, ra);
}

    private String updateStatus(Long appId, ApplicationStatus status, String from,RedirectAttributes ra) {
        var appOpt = applicationRepository.findById(appId);
        if (appOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบใบสมัคร");
            return "redirect:/teacher/jobs";
        }

        var app = appOpt.get();
        var job = app.getJob();
        if (job == null) {
            ra.addFlashAttribute("err", "ใบสมัครนี้ไม่ผูกกับงานแล้ว (ข้อมูลเสีย)");
            return "redirect:/teacher/jobs";
        }

        if (!job.getCreatorUsername().equals(SecUtil.currentUsername())) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์ปรับสถานะงานนี้");
            return "redirect:/teacher/jobs";
        }
        if (status == ApplicationStatus.REJECTED) {
            // 2. ให้ "จำ" สถานะ "ปัจจุบัน" (ก่อนที่จะเปลี่ยน) ไว้
            app.setRejectedAtStatus(app.getStatus()); 
        }

        app.setStatus(status);
        applicationRepository.save(app);

        try {
            // 4.1 ดึง User มาเตรียมไว้ (ทำแค่ครั้งเดียว)
            String applicantUsername = app.getApplicantUsername();
            var userOpt = userRepository.findByUsername(applicantUsername);

            if (userOpt.isEmpty()) {
                ra.addFlashAttribute("err", "ไม่พบผู้ใช้งาน: " + applicantUsername);
                return redirectByFrom(job.getId(), from);
            }

            var userToNotify = userOpt.get();
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
                    return "redirect:/teacher/applicants/" + job.getId();
            }
            notification.setDescription(notiDescription);
            notificationRepository.save(notification);
            
            if (userEmail != null && !userEmail.isBlank()) {
                try {
                    emailService.sendHtmlMessage(userEmail, subject, emailDescription);
                } catch (Exception mailErr) {
                    System.out.println("Email error: " + mailErr.getMessage());
                }
            }
        }

        catch (Exception e) {
            // จัดการ Error (ทำแค่ครั้งเดียว)
            ra.addFlashAttribute("err", "อัปเดตสถานะสำเร็จ แต่ส่ง Notification ล้มเหลว: " + e.getMessage());
        }
        ra.addFlashAttribute("msg", "อัปเดตสถานะเรียบร้อย");

        if (from == null) from = "";

        return redirectByFrom(job.getId(), from);
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
    public String interviewsPage(Model model) {
        String me = SecUtil.currentUsername();
        var jobs = jobRepository.findByCreatorUsernameOrderByCreatedAtDesc(me);

        model.addAttribute("jobs", jobs);
        return "teacher_interview";
    }

    @GetMapping("/interview/{jobId}")
    public String interviewList(@PathVariable Long jobId,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "department", required = false) String department,
                                Model model,
                                RedirectAttributes ra) {
    
        var jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบบันทึกงาน");
            return "redirect:/teacher/interview";
        }

        var job = jobOpt.get();

        if (!job.getCreatorUsername().equals(SecUtil.currentUsername())) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์เข้าถึงงานนี้");
            return "redirect:/teacher/interview";
        }

        List<ApplicationStatus> statusFilter = List.of(
            ApplicationStatus.APPROVED,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.REJECTED
        );

        List<Application> apps = applicationRepository
            .findByJobIdAndStatusInOrderByAppliedAtDesc(jobId, statusFilter);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase();
            apps = apps.stream()
                    .filter(a ->
                           (a.getFullName() != null && a.getFullName().toLowerCase().contains(kw)) ||
                           (a.getStudentId() != null && a.getStudentId().toLowerCase().contains(kw))
                    )
                    .toList();
        }

        if (department != null && !department.equals("ALL")) {
            apps = apps.stream()
                    .filter(a -> a.getDepartment() != null && a.getDepartment().equalsIgnoreCase(department))
                    .toList();
        }

        model.addAttribute("job", job);
        model.addAttribute("apps", apps);
        model.addAttribute("keyword", keyword);
        model.addAttribute("department", department);
        model.addAttribute("departments", job.getDepartments());

        return "teacher_interview_list";
    }

    @GetMapping("/interview/{jobId}/detail/{appId}")
    public String interviewDetail(@PathVariable Long jobId,
                                  @PathVariable Long appId,
                                  Model model,
                                  RedirectAttributes ra) {
    
        var jobOpt = jobRepository.findById(jobId);
        var appOpt = applicationRepository.findById(appId);

        if (jobOpt.isEmpty() || appOpt.isEmpty()) {
            ra.addFlashAttribute("err", "ไม่พบข้อมูล");
            return "redirect:/teacher/interview";
        }

        var job = jobOpt.get();
        var app = appOpt.get();

        if (!job.getCreatorUsername().equals(SecUtil.currentUsername())) {
            ra.addFlashAttribute("err", "ไม่มีสิทธิ์เข้าถึง");
            return "redirect:/teacher/interview";
        }

        model.addAttribute("job", job);
        model.addAttribute("app", app);
        return "teacher_interview_detail";
    }

    @GetMapping("/final")
    public String finalPage() {
        return "teacher_final";
    }
}