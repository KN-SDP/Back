package com.knusdp.SmartLedger.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SaveUserLoginInfoDto {
    private String userEmail;
    private String userPassword;
    private String checkedPassword;
    private String userName;
    private String userNickName;
    private String userBirth;
    private String userPhoneNumber;
}
