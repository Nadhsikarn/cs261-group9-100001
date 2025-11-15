package com.example.campusjobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class CampusJobsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusJobsApplication.class, args);
    }
}
