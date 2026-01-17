package com.kbhc.codetest.api.auth.service;

import com.kbhc.codetest.api.auth.jwt.JwtTokenProvider;
import com.kbhc.codetest.dto.jwt.JwtToken;
import com.kbhc.codetest.dto.jwt.JwtTokenRequest;
import com.kbhc.codetest.dto.auth.request.RequestMemberLogin;
import com.kbhc.codetest.exception.NotFoundException;
import com.kbhc.codetest.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisService redisService;

    /**
     * 사용자 로그인 처리
     * @param request 로그인 정보
     * @return 토큰정보
     * 1. 사용자 로그인 시도
     * 2. 사용자 인증 처리
     * 3. 인증정보를 바탕으로 토큰 생성
     * 4. 리프레시 토큰을 redis에 저장
     */

    @Transactional
    public JwtToken login(RequestMemberLogin request) {

        // 1. 사용자 인증정보 설정
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication;
        try {
            // 2. 사용자 인증
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패: 잘못된 자격증명, email={}", request.getEmail());
            throw e;
        } catch (Exception e) {
            log.error("로그인 실패: 알수 없는 에러, email={}", request.getEmail(), e);
            throw e;
        }

        // 3. 인증정보를 바탕으로 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        try {
            // 4. refresh token redis에 저장
            redisService.setRefreshToken(request.getEmail(),
                            jwtToken.getRefreshToken(),
                            jwtToken.getRefreshTokenExpirationTime());
        } catch (DataAccessException e) {
            log.error("Redis에 리프레시 토큰 저장 실패, key={}", "RT:" + request.getEmail(), e);
            throw e;
        }
        return jwtToken;
    }

    @Transactional
    public JwtToken reissue(JwtTokenRequest request) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
        }

        // 2. access Token에서 Email 추출
        String email = jwtTokenProvider.getEmailFromExpiredAccessToken(request.getAccessToken());

        // 3. Redis에 저장된 Refresh Token과 일치하는지 확인
        String savedRefreshToken = redisService.getRefreshToken(email);
        if (!request.getRefreshToken().equals(savedRefreshToken)) {
            throw new RuntimeException("리프레시 토큰 정보가 일치하지 않거나 로그아웃된 사용자 입니다.");
        }

        // 4. 새로운 토큰 생성
        if(memberRepository.existsByEmail(email)) {
            // 만료된 엑세스 토큰에서 권한정보 불러옴
            String authorities = jwtTokenProvider.getAuthoritiesFromExpiredAccessToken(request.getAccessToken());
            JwtToken newToken = jwtTokenProvider.reissueToken(email, authorities);
            // 5. Redis에 리프레시 토큰 정보 업데이트
            try {
                redisService.setRefreshToken(email,
                        newToken.getRefreshToken(),
                        newToken.getRefreshTokenExpirationTime());
            }
            catch (DataAccessException e) {
                log.error("Redis에 리프레시 토큰 저장 실패, key={}", "RT:"+email, e);
                throw e;
            }
            log.debug("새로운 토큰 발급 완료 : {}", newToken);
            return newToken;
        }
        else {
            throw new NotFoundException("가입되지 않은 사용자 입니다.");
        }
    }

    /**
     * 로그아웃 처리
     * @param accessToken
     * 1. 유효한 토큰 인지 검증
     * 2. 리프레시 토큰 삭제
     * 3. 액세스 토큰 블랙리스트 처리
     */
    @Transactional
    public void logout(String accessToken) {
        // FIXME. 예외 처리 추가할 것
        // 토큰 validation
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("빈 토큰으로 로그아웃 요청됨");
            return;
        }

        Authentication authentication;
        try {
            // 인증 정보 조회
            authentication = jwtTokenProvider.getAuthentication(accessToken);
        } catch (Exception e) {
            // 이미 만료되었거나 위조된 토큰일 수 있음
            log.warn("로그아웃 실패: 유효하지 않은 엑세스 토큰입니다.", e);
            return;
        }

        try {
            // refresh token 조회
            String storedRefreshToken = redisService.getRefreshToken(authentication.getName());
            if (storedRefreshToken != null) {
                // 리프레시 토큰이 있으면 삭제처리
                redisService.deleteRefreshToken(authentication.getName());
                log.debug("Redis에 리프레시 토큰이 삭제되었습니다. key={}", "RT:" + authentication.getName());
            }
        } catch (DataAccessException e) {
            log.error("Redis에 리프레시 토큰 삭제 실패, key={}", "RT:" + authentication.getName(), e);
        }

        try {
            Long expiration = jwtTokenProvider.getExpiration(accessToken);
            if (expiration != null && expiration > 0) {
                redisService.setBlackList(accessToken, "logout", expiration);
                log.debug("엑세스 토큰이 블랙리스트 처리 되었습니다. 만료시간={}ms", expiration);
            }
        } catch (Exception e) {
            log.warn("엑세스 토큰 블랙리스트 처리 실패", e);
        }
    }
}
