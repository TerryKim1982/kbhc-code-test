package com.kbhc.codetest.api.member.service;

import com.kbhc.codetest.dto.ApiResponse;
import com.kbhc.codetest.dto.member.request.RequestMemberJoin;
import com.kbhc.codetest.entity.member.Member;
import com.kbhc.codetest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;


    public ResponseEntity<?> join(RequestMemberJoin member) {
        // 이메일로 이미 가입되어있는지 확인
        if(isAlreadyMember(member.getEmail())) {
            // FIXME. 나중에 익셉션 추가해서 수정
            throw new RuntimeException("이미 가입되어 있는 사용자입니다.");
        }
        // 회원가입 절차 진행
        LocalDateTime now = LocalDateTime.now();
        Member newMember = Member.builder().
                email(member.getEmail()).
                password(passwordEncoder.encode(member.getPassword())).
                name(member.getName()).
                nickname(member.getEmail()).
                createdAt(now).
                updatedAt(now).build();
        memberRepository.save(newMember);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 성공적으로 완료되었습니다."));
    }

    private boolean isAlreadyMember(String email) {
        return memberRepository.existsByEmail(email);
    }
}
