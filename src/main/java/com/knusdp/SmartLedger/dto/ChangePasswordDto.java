package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {
    private String CurrentPassword;
    private String NewPassword;
    private String CheckedPassword;
}
