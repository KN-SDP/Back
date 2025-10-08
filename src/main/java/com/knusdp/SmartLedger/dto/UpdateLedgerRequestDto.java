package com.knusdp.SmartLedger.dto;

import com.knusdp.SmartLedger.entity.PaymentType;
import com.knusdp.SmartLedger.entity.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateLedgerRequestDto {
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private TransactionType transactionType;
    private PaymentType paymentType;
    private Long categoryId;
}