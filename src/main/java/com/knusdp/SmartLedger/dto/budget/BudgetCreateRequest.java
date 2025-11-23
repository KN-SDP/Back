package com.knusdp.SmartLedger.dto.budget;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BudgetCreateRequest {

    private Long categoryId;
    private Long amount;
}