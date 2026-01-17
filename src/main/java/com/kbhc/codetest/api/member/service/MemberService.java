package com.kbhc.codetest.api.member.service;

import com.kbhc.codetest.dto.ApiResponse;
import com.kbhc.codetest.dto.member.request.RequestMemberJoin;
import com.kbhc.codetest.entity.member.Member;
import com.kbhc.codetest.exception.DuplicateException;
import com.kbhc.codetest.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> join(RequestMemberJoin request) {
        // 이메일로 이미 가입되어있는지 확인
        if(isAlreadyMember(request.getEmail())) {
            throw new DuplicateException("이미 가입되어 있는 사용자입니다.");
        }
        // 회원가입 절차 진행
        OffsetDateTime now = OffsetDateTime.now();
        Member newMember = Member.builder().
                email(request.getEmail()).
                password(passwordEncoder.encode(request.getPassword())).
                name(request.getName()).
                nickname(request.getNickname()).
                createdAt(now).
                updatedAt(now).build();
        memberRepository.save(newMember);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 성공적으로 완료되었습니다."));
    }

    private boolean isAlreadyMember(String email) {
        return memberRepository.existsByEmail(email);
    }
}
