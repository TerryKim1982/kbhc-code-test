package com.kbhc.codetest.api.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * jwt 필터에서 발생하는 예외 처리
 */

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        // 유효한 자격증명을 제공하지 않고 접근하려 할 때 401 Unauthorized 에러 전달
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 정보가 유효하지 않습니다.");
    }
}
