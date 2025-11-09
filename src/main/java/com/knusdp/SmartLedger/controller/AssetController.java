package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.asset.*;
import com.knusdp.SmartLedger.service.AssetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AssetController {


    private final AssetService assetService;

    //현금, 은행 추가
    @PostMapping("/liquid")
    public ResponseEntity<Map<String, Object>> createLiquidAsset(
            @Valid @RequestBody CreateLiquidAssetRequestDto dto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        Long id = assetService.createLiquidAsset(dto,userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "detailId", id,
                        "message", "자산이 등록되었습니다."
                ));
    }

    //코인, 주식 추가
    @PostMapping("/investment")
    public ResponseEntity<Map<String, Object>> createInvestmentAsset(
            @Valid @RequestBody CreateInvestmentAssetRequestDto dto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status_code", 401,
                            "error_code", "UNAUTHORIZED",
                            "message", "인증이 필요합니다."
                    ));
        }

        Long userId = Long.parseLong(authentication.getName());
        Long id = assetService.createInvestmentAsset(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "detailId", id,
                        "message", "자산이 등록되었습니다."
                ));
    }

    //자산 검색
    @GetMapping
    public AssetListResponseDto getAssets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        return assetService.getAllAssets(userId);
    }

    @GetMapping("/{type}")
    public AssetListResponseDto getAssetsByType(@PathVariable String type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        return assetService.getAssetsByType(userId, type);
    }

    //자산 전체조회
    @GetMapping("/summary")
    public AssetSummaryResponseDto getAssetSummary() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        return assetService.getAssetSummary(userId);
    }
    //자산 이력 조회(날짜별 총자산)
    @GetMapping("/history")
    public AssetHistoryResponseDto getAssetHistory(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        return assetService.getAssetHistory(userId, start, end);
    }

    //코인, 주식 금액 수정
    @PatchMapping("/{assetId}/investment")
    public ResponseEntity<Map<String, String>> updateInvestmentAsset(
            @PathVariable Long assetId,
            @RequestBody UpdateInvestmentAssetDto dto
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        assetService.updateInvestmentAsset(userId, assetId, dto);

        return ResponseEntity.ok(Map.of("message", "자산 정보가 수정되었습니다."));
    }

    @PatchMapping("/{assetId}/liquid")
    public ResponseEntity<Map<String, String>> updateLiquidAsset(
            @PathVariable Long assetId,
            @RequestBody UpdateLiquidAssetDto dto
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        assetService.updateLiquidAsset(userId, assetId, dto);

        return ResponseEntity.ok(Map.of("message", "자산 정보가 수정되었습니다."));
    }

    //자산 삭제
    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        assetService.deleteAsset(userId, assetId);

        return ResponseEntity.noContent().build(); // 204 반환
    }

}
