package com.knusdp.SmartLedger.dto.goal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

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


    @NotNull(message = "시작일은 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;


    @NotNull(message = "마감일은 필수입니다.")
    @FutureOrPresent(message = "마감일은 오늘 또는 미래여야 합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    private MultipartFile image;
}