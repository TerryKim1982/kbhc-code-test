package com.kbhc.codetest.api.auth.controller;

import com.kbhc.codetest.api.auth.jwt.dto.JwtToken;
import com.kbhc.codetest.api.auth.service.AuthService;
import com.kbhc.codetest.dto.auth.request.RequestMemberLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody RequestMemberLogin request) {
        JwtToken token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    // 로그아웃 (헤더에 Bearer 토큰)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Authorization 헤더에 Bearer 토큰이 필요합니다.");
        }
        String accessToken = authorization.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
