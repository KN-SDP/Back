package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
//아이디 찾기 요청받기 dto
public class FindIdRequestDto {
    private String username;
    private String phoneNum;
    private String birth;
}
