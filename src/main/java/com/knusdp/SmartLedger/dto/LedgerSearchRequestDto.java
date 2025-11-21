package com.knusdp.SmartLedger.dto;

import com.knusdp.SmartLedger.entity.TransactionType;
import lombok.Data;
//통합 조회 파라미터 Dto
@Data
public class LedgerSearchRequestDto {
    private Integer year;
    private Integer month;
    private Integer day;
    private String categoryName;
    private TransactionType transactionType;
}