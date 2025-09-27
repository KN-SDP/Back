package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LedgerService {
    private final LedgerRepository ledgerRepository;

    public AccountBook createLedgerEntry(CreateAccountDto dto){
        if (dto.getDate() == null || dto.getDescription() == null ||
                dto.getAmount() == null || dto.getTransactionType() == null ||
                dto.getPaymentType() == null || dto.getCategory() == null) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }
        AccountBook accountBook = AccountBook.builder()
                .transactionDate(dto.getDate())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .transactionType(dto.getTransactionType())
                .paymentType(dto.getPaymentType())
                .categoryId(dto.getCategory())
                .build();
        return ledgerRepository.save(accountBook);
    }
}
