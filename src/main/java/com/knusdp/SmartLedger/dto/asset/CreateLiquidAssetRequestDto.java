package com.knusdp.SmartLedger.dto.asset;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLiquidAssetRequestDto {
    private String type;    // CASH / BANK
    private String name;    // 국민은행, 현금 등
    private Long amount;    // 금액
}
