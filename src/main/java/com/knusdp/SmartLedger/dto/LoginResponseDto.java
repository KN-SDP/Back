package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private Long userId;
    private String email;
    private String username;
    private String nickname;
    private String token; // JWT 토큰
}
