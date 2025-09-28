package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.exception.MissingRequiredFieldException;
import com.knusdp.SmartLedger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class LedgerService {
    private final LedgerRepository ledgerRepository;

    public AccountBook createLedgerEntry(CreateAccountDto dto){
        if (dto.getDate() == null || dto.getDescription() == null ||
                dto.getAmount() == null || dto.getTransactionType() == null ||
                dto.getPaymentType() == null || dto.getCategory() == null) {
            throw new MissingRequiredFieldException("필수 입력값이 누락되었습니다.");
        }
        if (dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("금액은 0보다 커야 합니다.");
        }
        //TransactionType은 열거형이라 예외처리는 널값만 확인하면 된대

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
