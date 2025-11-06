package com.knusdp.SmartLedger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO
@Getter
@Setter
@NoArgsConstructor
public class ChangeNicknameDto {
    @NotBlank(message = "닉네임은 비워둘 수 없습니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String change_nickname;
}
