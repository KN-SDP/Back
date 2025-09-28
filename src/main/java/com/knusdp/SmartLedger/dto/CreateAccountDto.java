package com.knusdp.SmartLedger.dto;

import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.PaymentType;
import com.knusdp.SmartLedger.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
//지출 추가 dto
public class CreateAccountDto {
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private TransactionType transactionType;
    private PaymentType paymentType;
    private Long categoryId;

}
