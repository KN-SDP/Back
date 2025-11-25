package com.knusdp.SmartLedger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateGoalRequestDto {

    @Schema(type = "string", example = "목표 제목")
    @NotBlank
    private String title;

    @Schema(type = "number", example = "10000")
    @NotNull
    @DecimalMin("1")
    private BigDecimal targetAmount;

    @Schema(type = "string", format = "date", example = "2025-11-25")
    @NotNull
    @FutureOrPresent
    private LocalDate deadline;

    private MultipartFile image;
}

