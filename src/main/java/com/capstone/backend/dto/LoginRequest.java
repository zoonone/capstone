package com.capstone.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {

    private static final String INVALID_CREDENTIALS_MESSAGE = "이메일 혹은 비밀번호가 잘못되었습니다.";

    @NotBlank(message = INVALID_CREDENTIALS_MESSAGE)
    @Email(message = INVALID_CREDENTIALS_MESSAGE)
    private String email;

    @NotBlank(message = INVALID_CREDENTIALS_MESSAGE)
    @Size(min = 4, message = INVALID_CREDENTIALS_MESSAGE)
    private String password;

}
