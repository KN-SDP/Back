package com.knusdp.SmartLedger.dto.accountBook;

import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.PaymentType;
import com.knusdp.SmartLedger.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LedgerResponseDto {
    private Long id;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private PaymentType paymentType;
    private String category; // 카테고리 이름을 String으로 전달
    private Long goalId;
    private LocalDateTime createAt;

    // 엔티티를 DTO로 변환하는 생성자
    public LedgerResponseDto(AccountBook accountBook) {
        this.id = accountBook.getTransactionId();
        this.date = accountBook.getTransactionDate();
        this.description = accountBook.getDescription();
        this.amount = accountBook.getAmount();
        this.type = accountBook.getTransactionType();
        this.paymentType = accountBook.getPaymentType();
        this.category = accountBook.getCategory().getCategoryName(); // 연관된 카테고리 객체에서 이름만 추출
        this.createAt = accountBook.getCreatedAt();
        if (accountBook.getGoal() != null) {
            this.goalId = accountBook.getGoal().getGoalId();
        } else {
            this.goalId = null;
        }
    }
}