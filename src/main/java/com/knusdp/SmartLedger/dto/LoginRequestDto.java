package com.knusdp.SmartLedger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON -> Object 변환을 위해 기본 생성자가 필요합니다.
public class LoginRequestDto {
    private String email;
    private String password;
}