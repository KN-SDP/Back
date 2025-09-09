package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInFoDto {
    private int userID;
    private String userEmail;
    private String userNickName;
    private String userName;
    private String userPhoneNum;
    private String userBirth;
}
