package com.kbhc.codetest.api.auth.service;

import com.kbhc.codetest.api.auth.jwt.JwtTokenProvider;
import com.kbhc.codetest.api.auth.jwt.dto.JwtToken;
import com.kbhc.codetest.dto.auth.request.RequestMemberLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate; // Redis 호출

    @Transactional
    public JwtToken login(RequestMemberLogin request) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), jwtToken.getRefreshToken(), jwtToken.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return jwtToken;
    }

    @Transactional
    public void logout(String accessToken) {
        // Access Token에서 User email 추출
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        // Redis에서 해당 User email로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // Access Token 블랙리스트 처리 (남은 유효시간만큼 Redis에 저장하여 접근 막음)
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue()
                .set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }
}
