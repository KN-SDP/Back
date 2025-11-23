package com.knusdp.SmartLedger.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class BudgetListResponseDto {
    private List<BudgetResponseDto> budgets;
}