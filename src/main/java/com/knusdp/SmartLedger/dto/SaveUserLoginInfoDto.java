package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveUserLoginInfoDto {
    private String userEmail;
    private String userPassword;
    private String checkedPassword;
    private String userName;
    private String userNickname;
    private String userBirth;
    private String userPhoneNumber;
}
