package com.kbhc.codetest.api.auth.controller;

import com.kbhc.codetest.api.auth.jwt.dto.JwtToken;
import com.kbhc.codetest.api.auth.service.AuthService;
import com.kbhc.codetest.dto.auth.request.RequestMemberLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody RequestMemberLogin request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 로그아웃 (헤더에 AccessToken 필수)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        // "Bearer " 제거
        String actualToken = token.substring(7);
        authService.logout(actualToken);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
