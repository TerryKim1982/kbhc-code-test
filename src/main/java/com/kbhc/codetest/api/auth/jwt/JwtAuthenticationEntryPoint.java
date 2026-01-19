package com.kbhc.codetest.api.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbhc.codetest.dto.ApiResponse;
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
        String exception = (String) request.getAttribute("exception");

        // 유효한 자격증명을 제공하지 않고 접근하려 할 때 401 Unauthorized 에러 전달
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Object> apiResponse;

        if ("REISSUE".equals(exception)) {
            // Access만 죽고 Refresh는 살아있는 경우
            apiResponse = ApiResponse.error("Access Token이 만료되었습니다. 재발급이 필요합니다.", "AUTH_REISSUE", 401);
        } else if ("EXPIRED".equals(exception)) {
            // 둘 다 죽은 경우
            apiResponse = ApiResponse.error("모든 토큰이 만료되었습니다. 다시 로그인해주세요.", "AUTH_TOKEN_EXPIRED", 401);
        } else {
            // 토큰 자체가 잘못된 경우 등
            apiResponse = ApiResponse.error("유효하지 않은 인증 정보입니다.", "AUTH_INVALID", 401);
        }

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().print(mapper.writeValueAsString(apiResponse));
    }
}
