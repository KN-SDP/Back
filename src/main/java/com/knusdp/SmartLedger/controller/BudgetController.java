package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.budget.*;
import com.knusdp.SmartLedger.service.BudgetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetCreateResponse> createBudget(
            @RequestBody BudgetCreateRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        BudgetCreateResponse response = budgetService.createBudget(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BudgetListResponseDto> getBudgets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        return ResponseEntity.ok(budgetService.getBudgets(userId));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetUpdateResponseDto> updateBudget(
            @PathVariable Long budgetId,
            @RequestBody BudgetUpdateRequestDto request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        BudgetUpdateResponseDto response = budgetService.updateBudget(budgetId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long budgetId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        budgetService.deleteBudget(budgetId, userId);
        return ResponseEntity.noContent().build();
    }
}
