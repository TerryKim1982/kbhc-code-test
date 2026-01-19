package com.kbhc.codetest.config;

import com.kbhc.codetest.api.auth.jwt.JwtAccessDeniedHandler;
import com.kbhc.codetest.api.auth.jwt.JwtAuthenticationEntryPoint;
import com.kbhc.codetest.api.auth.jwt.JwtAuthenticationFilter;
import com.kbhc.codetest.api.auth.jwt.JwtTokenProvider;
import com.kbhc.codetest.api.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final RedisService redisService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인은 인증 없이 허용
                        .requestMatchers("/api/v1/member/join").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                    // access Token 만료시 reissue 처리 부분
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 에러 핸들링 등록
                    // 접근 권한 부족 exception handler
                    .accessDeniedHandler(jwtAccessDeniedHandler)       // 403: 인가 실패 (권한 부족)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisService), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
