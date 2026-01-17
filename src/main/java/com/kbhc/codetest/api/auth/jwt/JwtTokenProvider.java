package com.kbhc.codetest.api.auth.jwt;

import com.kbhc.codetest.api.auth.jwt.dto.JwtToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    @Value("${jwt.token.access.expired.time}")
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.token.refresh.expired.time}")
    private long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성 (Access, Refresh)
    public JwtToken generateToken(Authentication authentication) {
        long now = (new Date()).getTime();
        // Access Token: 60분
        String accessToken = this.createAccessToken(authentication.getName(), now);
        // Refresh Token: 8시간
        String refreshToken = this.createRefreshToken(now);

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(refreshTokenValidityInMilliseconds)
                .build();
    }

    public JwtToken reissueToken(String email) {
        long now = (new Date()).getTime();
        String accessToken = this.createAccessToken(email, now);
        String refreshToken = this.createRefreshToken(now);
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(refreshTokenValidityInMilliseconds)
                .build();
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Access Token의 남은 유효 시간(ms)을 계산하여 반환
     */
    public Long getExpiration(String accessToken) {
        // 1. 토큰에서 모든 Claims 추출
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key) // JwtTokenProvider 생성 시 초기화된 secret key
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        // 2. 만료 시간 추출
        Date expiration = claims.getExpiration();

        // 3. 현재 시간 추출
        long now = new Date().getTime();

        // 4. (만료 시간 - 현재 시간)을 계산하여 남은 ms 반환
        return (expiration.getTime() - now);
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String createAccessToken(String email, long now) {
        return Jwts.builder()
                .setSubject(email)
                .claim("auth", "")
                .setExpiration(new Date(now + accessTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(long now) {
        return Jwts.builder()
                .setExpiration(new Date(now + refreshTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}