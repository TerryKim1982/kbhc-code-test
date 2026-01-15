package com.kbhc.codetest.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // 값 가져오기 (블랙리스트 확인용)
    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 로그아웃 시 블랙리스트에 추가하는 메서드 (참고용)
    public void setBlackList(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
    }
}
