package com.knusdp.SmartLedger.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverPasswordRequestDto {
    private String email;
    private String name;
    private String birth; // "yyyy-MM-dd" 형식
    private String phone;
}