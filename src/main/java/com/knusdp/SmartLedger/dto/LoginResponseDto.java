package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
//로그인 응답 dto
@Getter
@Builder
@NoArgsConstructor
public class LoginResponseDto {
    private String accessToken; // JWT 토큰

    public LoginResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
