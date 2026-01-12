package com.kbhc.codetest.api.member.service;

import com.kbhc.codetest.dto.member.request.RequestMemberJoin;
import com.kbhc.codetest.dto.member.response.ResponseMemberJoin;
import com.kbhc.codetest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public ResponseMemberJoin join(RequestMemberJoin member) {
        // 이메일로 이미 가입되어있는지 확인
        if(isAlreadyMember(member.getEmail())) {
            // FIXME. 나중에 익셉션 추가해서 수정
            throw new RuntimeException("이미 가입되어 있는 사용자입니다.");
        }
        // 회원가입 절차 진행
        // 비밀번호 암호화 해서
        // DB INSERT
        return null;
    }

    private boolean isAlreadyMember(String email) {
        return memberRepository.existsByEmail(email);
    }
}
