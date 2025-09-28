package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.exception.MissingRequiredFieldException;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
import com.knusdp.SmartLedger.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class AccountBookService {
    private final AccountBookRepository accountBookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createLedgerEntry(Long memberId, CreateAccountDto dto){
        if (dto.getDate() == null || dto.getDescription() == null ||
                dto.getAmount() == null || dto.getTransactionType() == null ||
                dto.getPaymentType() == null || dto.getCategoryId() == null) {
            throw new MissingRequiredFieldException("필수 입력값이 누락되었습니다.");
        }

        // 금액 0 이하 체크
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("금액은 0보다 커야 합니다.");
        }

        // 소수점 입력 체크
        if (dto.getAmount().scale() > 0) {
            throw new InvalidAmountException("금액은 정수만 입력할 수 있습니다.");
        }

        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        AccountCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        AccountBook accountBook = AccountBook.builder()
                .transactionDate(dto.getDate())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .transactionType(dto.getTransactionType())
                .paymentType(dto.getPaymentType())
                .category(category)
                .member(member)
                .build();

        accountBookRepository.save(accountBook);
    }

}
