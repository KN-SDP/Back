package com.knusdp.SmartLedger.dto;

import com.knusdp.SmartLedger.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateGoalRequestDto {
    // 모든 필드는 선택 사항입니다.
    private String title;
    private String imageUrl;

    @DecimalMin(value = "1", message = "목표 금액은 0보다 커야 합니다.")
    private BigDecimal targetAmount;

    @DecimalMin(value = "0", message = "현재 금액은 0 이상이어야 합니다.")
    private BigDecimal currentAmount;


    private LocalDate startDate;

    @FutureOrPresent(message = "마감일은 오늘 또는 미래여야 합니다.")
    private LocalDate deadline;

    private GoalStatus status; // 상태를 직접 변경할 수도 있게 추가 (선택 사항)
}