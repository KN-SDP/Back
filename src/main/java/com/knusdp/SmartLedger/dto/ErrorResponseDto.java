package com.knusdp.SmartLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    private int statusCode;
    private String errorCode;
    private String message;
}
