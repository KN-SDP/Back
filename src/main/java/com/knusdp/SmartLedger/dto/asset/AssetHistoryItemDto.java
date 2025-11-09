package com.knusdp.SmartLedger.dto.asset;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AssetHistoryItemDto {
    private LocalDate recordedAt;
    private Long totalAmount;
    private Double changeRate;
}
