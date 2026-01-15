package com.kbhc.codetest.api.member.controller;

import com.kbhc.codetest.api.member.service.MemberService;
import com.kbhc.codetest.dto.member.request.RequestMemberJoin;
import com.kbhc.codetest.dto.auth.request.RequestMemberLogin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping(value = "/join")
    public ResponseEntity<?> join(@Valid @RequestBody RequestMemberJoin request) {
        return memberService.join(request);
    }
}
