package com.kbhc.codetest.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberLoginRequest {

    @Email
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "패스워드는 필수입니다.")
    private String password;
}
