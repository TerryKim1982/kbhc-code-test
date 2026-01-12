package com.kbhc.codetest.dto.member.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestMemberJoin {
    private String email;
    private String password;
    private String name;
    private String nickname;
}
