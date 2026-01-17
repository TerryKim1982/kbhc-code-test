package com.kbhc.codetest.api.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
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

        JSONObject responseJson = new JSONObject();

        if ("EXPIRED_TOKEN".equals(exception)) {
            responseJson.put("code", "401");
            responseJson.put("message", "토큰이 만료되었습니다. 재발급이 필요합니다.");
        } else if ("INVALID_TOKEN".equals(exception)) {
            responseJson.put("code", "401");
            responseJson.put("message", "유효하지 않은 토큰입니다.");
        } else {
            responseJson.put("code", "401");
            responseJson.put("message", "인증 정보가 없습니다.");
        }

        response.getWriter().print(responseJson);
    }
}
