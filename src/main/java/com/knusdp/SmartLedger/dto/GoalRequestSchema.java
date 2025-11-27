package com.knusdp.SmartLedger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class GoalRequestSchema {
    @Schema(
            description = "JSON 데이터 (CreateGoalRequestDto)",
            type = "string",
            example = "{\"title\":\"string\",\"targetAmount\":1,\"deadline\":\"2025-11-23\"}"
    )
    private String data; // ← 반드시 String이어야 함

    @Schema(description = "업로드할 이미지 파일", type = "string", format = "binary")
    private MultipartFile image;
}

