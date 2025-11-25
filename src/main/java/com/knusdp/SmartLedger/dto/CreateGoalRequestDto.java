package com.knusdp.SmartLedger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGoalRequestDto {

    @NotBlank(message = "목표 제목은 필수입니다.")
    private String title;

    @NotNull(message = "목표 금액은 필수입니다.")
    @DecimalMin(value = "1", message = "목표 금액은 0보다 커야 합니다.")
    private BigDecimal targetAmount;

    @NotNull(message = "마감일은 필수입니다.")
    @FutureOrPresent(message = "마감일은 오늘 또는 미래여야 합니다.")
    private LocalDate deadline;
}