package com.knusdp.SmartLedger.dto.asset;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateInvestmentAssetRequestDto {
    private String type;        // STOCK, COIN
    private String name;        // 종목명
    private BigDecimal quantity; // 수량
    private Long avgPrice;      // 평균 매수가
}
