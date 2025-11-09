package com.knusdp.SmartLedger.dto.asset;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AssetSummaryResponseDto {
    private Long totalAmount;
    private Double changeRate;
    private Long changeAmount;
    private Map<String, Long> details;
}
