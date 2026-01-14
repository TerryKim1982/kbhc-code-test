package com.kbhc.codetest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화 (개발 환경용)
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인 경로는 인증 없이 허용
                        .requestMatchers("/api/v1/member/join").permitAll()
                        .requestMatchers("/api/v1/member/login").permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
