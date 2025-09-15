package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {
    private Long userId;
    private String token;
    private String userEmail;
    private String userPassword;

    public LoginRequestDto(Long userId, String email, String password, String token) {
        this.token = token;
        this.userId = userId;
        this.userPassword = password;
        this.userEmail = email;
    }
}
