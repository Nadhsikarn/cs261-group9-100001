package com.example.campusjobs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.campusjobs.service.DatabaseUserDetailsService;

@Configuration
public class SecurityConfig {

    private final TuAuthenticationProvider tuAuthProvider;
    private final DatabaseUserDetailsService dbUserDetailsService;

    public SecurityConfig(TuAuthenticationProvider tuAuthProvider, DatabaseUserDetailsService dbUserDetailsService) {
        this.tuAuthProvider = tuAuthProvider;
        this.dbUserDetailsService = dbUserDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        // สร้าง "ยาม" สำหรับ user ในระบบ (ทั้ง admin และ user ใน DB)
        // ยามคนนี้จะไปเรียกใช้ DatabaseUserDetailsService ของเรา
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setUserDetailsService(dbUserDetailsService);
        daoAuthProvider.setPasswordEncoder(passwordEncoder);
        
        // ProviderManager จะถาม `daoAuthProvider` (ที่ดู DB) ก่อน
        // ถ้าหา user ไม่เจอ (UsernameNotFoundException) ถึงจะไปถาม `tuAuthProvider` (ยาม API) ต่อ
        return new ProviderManager(daoAuthProvider, tuAuthProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .authenticationManager(authenticationManager)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                     "/", 
                    "/css/**", 
                "/js/**", 
                "/image/**", 
                "/jobUploads/**",
                "/jobs", 
                "/jobs/*"
            ).permitAll()
            .requestMatchers("/login", "/role").permitAll()
            .requestMatchers("/jobs/*/apply", "/student/**").hasAuthority("ROLE_STUDENT")
            .requestMatchers("/teacher/**").hasAuthority("ROLE_TEACHER")
            .anyRequest().authenticated()
        )
            .formLogin(form -> form
                .loginPage("/role")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
            .csrf(Customizer.withDefaults());

        return http.build();
    }
}
