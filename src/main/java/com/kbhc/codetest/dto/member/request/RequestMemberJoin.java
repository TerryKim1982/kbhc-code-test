package com.kbhc.codetest.dto.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestMemberJoin {

    @Email
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;
    @NotBlank(message = "패스워드는 필수입니다.")
    private String password;
    @NotBlank(message = "이름은 필수입니다.")
    private String name;
    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
}
