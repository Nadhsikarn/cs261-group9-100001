package com.example.campusjobs.controller;

import com.example.campusjobs.repo.ApplicationRepository;
import com.example.campusjobs.repo.JobRepository;
import com.example.campusjobs.util.SecUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public HomeController(JobRepository jobRepository,
                          ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/")
    public String home(Model model) {

        if (SecUtil.hasRole("TEACHER")) {

            String me = SecUtil.currentUsername();


            int allPosts = jobRepository.countByCreatorUsername(me);


            int dbActive = jobRepository.countByCreatorUsernameAndCloseDateAfter(me, LocalDate.now());
            int activePosts = dbActive + 2;   // บวก default showcase 2 งาน


            int newApplicants = applicationRepository.countNewApplicants(me);

            var applicantJobList = jobRepository.findByCreatorUsernameOrderByCreatedAtDesc(me);


            model.addAttribute("allPosts", allPosts);
            model.addAttribute("activePosts", activePosts);
            model.addAttribute("newApplicants", newApplicants);
            model.addAttribute("applicantJobList", applicantJobList);

        } else {
            // นักศึกษาเห็นงานทั้งหมด
            model.addAttribute("jobs", jobRepository.findAll());
        }

        return "index"; 
    }
}
