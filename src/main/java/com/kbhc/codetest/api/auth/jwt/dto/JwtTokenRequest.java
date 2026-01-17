package com.kbhc.codetest.api.auth.jwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtTokenRequest {
    private String accessToken;
    private String refreshToken;
}
