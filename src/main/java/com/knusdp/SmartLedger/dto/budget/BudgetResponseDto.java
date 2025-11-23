package com.knusdp.SmartLedger.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BudgetResponseDto {
    private Long categoryId;
    private Long budgetAmount;
    private Long usedAmount;
}