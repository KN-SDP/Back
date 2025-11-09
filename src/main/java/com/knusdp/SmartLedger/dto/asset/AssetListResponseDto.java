package com.knusdp.SmartLedger.dto.asset;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AssetListResponseDto {
    private List<AssetResponseDto> assets;
}
