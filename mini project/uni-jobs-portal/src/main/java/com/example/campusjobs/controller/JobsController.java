package com.example.campusjobs.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.campusjobs.model.Application;
import com.example.campusjobs.model.Notification;
import com.example.campusjobs.model.User;
import com.example.campusjobs.repo.ApplicationRepository;
import com.example.campusjobs.repo.JobRepository;
import com.example.campusjobs.repo.NotificationRepository;
import com.example.campusjobs.repo.UserRepository;
import com.example.campusjobs.util.SecUtil;

@Controller
@RequestMapping("/jobs")
public class JobsController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;

    public JobsController(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/preview")
        //public String preview(@PathVariable Long id, Model model)
        public String preview(@RequestParam Long id, Model model) {
        var job = jobRepository.findById(id).orElse(null);
        model.addAttribute("job", job); // ส่ง job เข้าไปใน preview
        return "job_preview"; // ต้องมีไฟล์ job_preview.html ใน /templates
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var job = jobRepository.findById(id).orElse(null);
        model.addAttribute("job", job);

        String me = SecUtil.currentUsername();
        boolean alreadyApplied = (me != null) && applicationRepository.existsByJobIdAndApplicantUsername(id, me);
        model.addAttribute("alreadyApplied", alreadyApplied);

        return "job_detail";
    }

    @PostMapping("/{id}/apply")
        public String apply(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String studentId,
            @RequestParam String email,
            @RequestParam String phone,
            RedirectAttributes ra) 
        {

    var job = jobRepository.findById(id).orElse(null);
    if (job == null) {
        ra.addFlashAttribute("err", "ไม่พบบันทึกงาน");
        return "redirect:/";
    }

    String me = SecUtil.currentUsername();

    // 🛡️ ห้าม Teacher สมัครงาน (ตรวจจาก username)
    if (me != null && me.toLowerCase().contains("teacher")) {
        ra.addFlashAttribute("err", "Teacher ไม่สามารถสมัครงานได้");
        return "redirect:/jobs/" + id;
    }

    if (applicationRepository.existsByJobIdAndApplicantUsername(id, me)) {
        ra.addFlashAttribute("err", "คุณสมัครงานนี้แล้ว");
        return "redirect:/jobs/" + id;
    }

    var app = new Application(job, me, fullName, studentId, email, phone );
    applicationRepository.save(app);
    try {
                // 4.1 ดึง Username (String) จาก Application
                String applicantUsername = app.getApplicantUsername();
                // 4.2 ค้นหา User object จาก Username
                Optional<User> userToNotify = userRepository.findByUsername(applicantUsername);
                // 4.3 ตรวจสอบว่าเจอ User
                if (userToNotify.isEmpty()) {
                    throw new Exception("ไม่พบผู้ใช้งาน " + applicantUsername);
                }
                // 4.4 ดึง ID (Long) ที่เราต้องการ
                Long applicantId = userToNotify.get().getId();
                // 4.5 สร้าง Notification
                Notification notification = new Notification();
                notification.setUserId(applicantId); // ⬅️ ใช้ ID (Long) ที่ถูกต้องแล้ว
                notification.setDescription("You’ve applied for Staff " + job.getTitle() + " successfully!");
                notification.setLinkUrl("/student/applications" + app.getId()); // (แก้ URL ให้ถูก)
                // 4.6 บันทึก Notification
                notificationRepository.save(notification);
            }
            catch (Exception e) {
                // ถ้าล้มเหลว ให้แจ้งเตือน แต่ยังคงทำงานต่อไป
                // (ในทางปฏิบัติ ควรใช้ logger บันทึก error)
                ra.addFlashAttribute("err", "อัปเดตสถานะสำเร็จ แต่ส่ง Notification ล้มเหลว: " + e.getMessage());
            }
    ra.addFlashAttribute("popupMsg", "Application Submitted Successfully!");
    return "redirect:/";

}

}
