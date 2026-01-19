package com.kbhc.codetest.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    // 값 가져오기 (블랙리스트 확인용)
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
    }

    // refresh token 저장용
    public void setRefreshToken(String email, String value, long expireTime) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + email, value, expireTime, TimeUnit.MILLISECONDS);
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }

    // 로그아웃 시 블랙리스트에 추가하는 메서드
    public void setBlackList(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
    }

    public boolean hasKey(String email) {
        return redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + email);
    }
}
