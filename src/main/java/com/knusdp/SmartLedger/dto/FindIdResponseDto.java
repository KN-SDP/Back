package com.knusdp.SmartLedger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//이메일(아이디정보) 리턴하는 dto
public class FindIdResponseDto {

    private int status_code;
    private String message;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String birth;
}
