package com.knusdp.SmartLedger.dto.asset;

import com.knusdp.SmartLedger.entity.AssetType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AssetResponseDto {
    private Long detailId;
    private AssetType type;
    private String name;
    private Long amount;
    private BigDecimal quantity;
    private BigDecimal avgPrice;
    private LocalDateTime updatedAt;
}
