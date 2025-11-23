package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.budget.*;
import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.Budget;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.exception.budget.BudgetAlreadyExistsException;
import com.knusdp.SmartLedger.exception.budget.BudgetNotFoundException;
import com.knusdp.SmartLedger.exception.budget.CategoryNotFoundException;
import com.knusdp.SmartLedger.exception.budget.NoBudgetEntriesException;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
import com.knusdp.SmartLedger.repository.BudgetRepository;
import com.knusdp.SmartLedger.repository.CategoryRepository;

import com.knusdp.SmartLedger.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;

    // 예산 생성
    public BudgetCreateResponse createBudget(BudgetCreateRequest request, Long userId) {

        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new InvalidAmountException("금액은 0 이상 숫자 형식이어야 합니다.");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        AccountCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("해당 카테고리를 찾을 수 없습니다."));

        if (budgetRepository.existsByMemberAndCategory(member, category)) {
            throw new BudgetAlreadyExistsException("해당 카테고리의 예산이 이미 존재합니다.");
        }

        Budget budget = Budget.builder()
                .member(member)
                .category(category)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .build();

        budgetRepository.save(budget);

        return new BudgetCreateResponse("예산이 생성되었습니다.");
    }

    // 예산 전체 조회
    @Transactional(readOnly = true)
    public BudgetListResponseDto getBudgets(Long userId) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Budget> budgets = budgetRepository.findAllByMember(member);

        if (budgets.isEmpty()) {
            throw new NoBudgetEntriesException("등록된 예산 정보가 없습니다.");
        }

        List<BudgetResponseDto> result = budgets.stream()
                .map(budget -> {
                    Long usedAmount = accountBookRepository.getUsedAmount(
                            member.getId(), budget.getCategory().getCategoryId()
                    );

                    return new BudgetResponseDto(
                            budget.getCategory().getCategoryId(),
                            budget.getAmount().longValue(),
                            usedAmount
                    );
                })
                .toList();

        return new BudgetListResponseDto(result);
    }

    // 예산 수정
    public BudgetUpdateResponseDto updateBudget(Long budgetId, BudgetUpdateRequestDto request, Long userId) {

        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new InvalidAmountException("금액은 0 이상 숫자 형식이어야 합니다.");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException("해당 예산 정보를 찾을 수 없습니다."));

        if (!budget.getMember().getId().equals(userId)) {
            throw new BudgetNotFoundException("해당 예산 정보를 찾을 수 없습니다.");
        }

        budget.setAmount(BigDecimal.valueOf(request.getAmount()));

        return new BudgetUpdateResponseDto("예산이 수정되었습니다.");
    }

    // 예산 삭제
    public void deleteBudget(Long budgetId, Long userId) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException("해당 예산 정보를 찾을 수 없습니다."));

        if (!budget.getMember().getId().equals(userId)) {
            throw new BudgetNotFoundException("해당 예산 정보를 찾을 수 없습니다.");
        }

        budgetRepository.delete(budget);
    }
}
