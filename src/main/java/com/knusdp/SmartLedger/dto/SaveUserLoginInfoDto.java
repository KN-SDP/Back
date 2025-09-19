package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

//회원가입 정보 받기 dto

public class SaveUserLoginInfoDto {
    private String userEmail;
    private String userPassword;
    private String checkedPassword;
    private String userName;
    private String userNickname;
    private String userBirth;
    private String userPhoneNumber;
}
