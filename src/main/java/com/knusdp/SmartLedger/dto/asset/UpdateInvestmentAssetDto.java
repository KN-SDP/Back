package com.knusdp.SmartLedger.dto.asset;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateInvestmentAssetDto {
    private BigDecimal quantity;
    private Long avgPrice;
}
