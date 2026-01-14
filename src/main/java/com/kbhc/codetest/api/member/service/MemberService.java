package com.kbhc.codetest.api.member.service;

import com.kbhc.codetest.dto.ApiResponse;
import com.kbhc.codetest.dto.member.request.RequestMemberJoin;
import com.kbhc.codetest.dto.member.request.RequestMemberLogin;
import com.kbhc.codetest.entity.member.Member;
import com.kbhc.codetest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;


    public ResponseEntity<?> join(RequestMemberJoin request) {
        // 이메일로 이미 가입되어있는지 확인
        if(isAlreadyMember(request.getEmail())) {
            // FIXME. 나중에 익셉션 추가해서 수정
            throw new RuntimeException("이미 가입되어 있는 사용자입니다.");
        }
        // 회원가입 절차 진행
        LocalDateTime now = LocalDateTime.now();
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

    public ResponseEntity<?> login(RequestMemberLogin request) {
        Optional<Member> memberOptional = memberRepository.findByEmail(request.getEmail());
        if(memberOptional.isEmpty()) {
            // FIXME. 나중에 익셉션 추가해서 수정
            throw new RuntimeException("존재하지 않는 사용자 이거나 비밀번호가 일치하지 않습니다.");
        }
        Member member = memberOptional.get();
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return ResponseEntity.ok(ApiResponse.success("로그인 성공"));
    }

    private boolean isAlreadyMember(String email) {
        return memberRepository.existsByEmail(email);
    }
}
