package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.asset.*;
import com.knusdp.SmartLedger.entity.*;
import com.knusdp.SmartLedger.exception.EmailValidationError;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.exception.MissingRequiredFieldException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.exception.asset.AssetNotFoundException;
import com.knusdp.SmartLedger.exception.asset.InvalidDateRangeException;
import com.knusdp.SmartLedger.repository.AssetHistoryRepository;
import com.knusdp.SmartLedger.repository.AssetRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetHistoryRepository assetHistoryRepository;
    private final MemberRepository memberRepository;

    //현금 은행 추가
    @Transactional
    public Long createLiquidAsset(CreateLiquidAssetRequestDto dto, Long userId) {

        //  필수 값 검증
        if (dto.getType() == null || dto.getName() == null || dto.getAmount() == null) {
            throw new MissingRequiredFieldException("필수 입력값이 누락되었습니다.");
        }

        //  ENUM 검증
        AssetType assetType;
        try {
            assetType = AssetType.valueOf(dto.getType());
        } catch (IllegalArgumentException e) {
            throw new EmailValidationError("자산유형은 CASH 또는 BANK 이어야 합니다.");
        }

        if (!(assetType == AssetType.CASH || assetType == AssetType.BANK)) {
            throw new EmailValidationError("자산유형은 CASH 또는 BANK 이어야 합니다.");
        }

        // 금액 검증
        if (dto.getAmount() <= 0) {
            throw new InvalidAmountException("금액은 0보다 커야 합니다.");
        }

        // Member 검증
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다."));

        //SuperBuilder 로 자식 빌더 사용
        AssetLiquid asset = AssetLiquid.builder()
                .assetType(assetType)
                .assetName(dto.getName())
                .amount(dto.getAmount())
                .member(member)
                .build();

        assetRepository.save(asset);

        return asset.getAssetId();
    }

    @Transactional
    public Long createInvestmentAsset(Long userId, CreateInvestmentAssetRequestDto dto) {

        // 1) 필수 값 검증
        if (dto.getType() == null || dto.getName() == null || dto.getQuantity() == null || dto.getAvgPrice() == null) {
            throw new MissingRequiredFieldException("필수 입력값이 누락되었습니다.");
        }

        // 2) Type 검증
        AssetType assetType;
        try {
            assetType = AssetType.valueOf(dto.getType());
        } catch (Exception e) {
            throw new InvalidAmountException("자산유형은 COIN, STOCK 중 하나여야 합니다.");
        }

        if (!(assetType == AssetType.COIN || assetType == AssetType.STOCK)) {
            throw new InvalidAmountException("자산유형은 COIN, STOCK 중 하나여야 합니다.");
        }

        // 3) 수량/가격 검증
        if (dto.getQuantity().compareTo(BigDecimal.ZERO) <= 0 || dto.getAvgPrice() <= 0) {
            throw new InvalidAmountException("금액은 0보다 커야 합니다.");
        }

        // 4) 사용자 검증
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 5) Entity 생성
        AssetInvestment asset = AssetInvestment.builder()
                .assetType(assetType)
                .assetName(dto.getName())
                .quantity(dto.getQuantity())
                .avgPrice(BigDecimal.valueOf(dto.getAvgPrice()))
                .member(member)
                .build();

        assetRepository.save(asset);

        return asset.getAssetId();
    }

    // 자산 검색
    public AssetListResponseDto getAllAssets(Long userId) {
        List<Asset> assets = assetRepository.findAllByMember_Id(userId);

        if (assets.isEmpty()) {
            throw new AssetNotFoundException("등록된 자산이 없습니다.");
        }

        List<AssetResponseDto> dtoList = assets.stream().map(asset -> {

            if (asset instanceof AssetLiquid liquid) {
                return AssetResponseDto.builder()
                        .detailId(liquid.getAssetId())
                        .type(liquid.getAssetType())
                        .name(liquid.getAssetName())
                        .amount(liquid.getAmount())
                        .updatedAt(liquid.getUpdateAt())
                        .build();
            }

            if (asset instanceof AssetInvestment invest) {
                Long amount = invest.getAvgPrice()
                        .multiply(invest.getQuantity())
                        .longValue();

                return AssetResponseDto.builder()
                        .detailId(invest.getAssetId())
                        .type(invest.getAssetType())
                        .name(invest.getAssetName())
                        .amount(amount)
                        .quantity(invest.getQuantity())
                        .avgPrice(invest.getAvgPrice())
                        .updatedAt(invest.getUpdateAt())
                        .build();
            }

            return null;

        }).collect(Collectors.toList());

        return AssetListResponseDto.builder()
                .assets(dtoList)
                .build();
    }

    // 상세조회
    public AssetListResponseDto getAssetsByType(Long userId, String type) {

        List<Asset> assets = assetRepository.findAllByMember_Id(userId);

        List<Asset> filtered = assets.stream()
                .filter(a -> a.getAssetType().name().equals(type))
                .toList();

        if (filtered.isEmpty()) {
            throw new AssetNotFoundException("해당 유형의 자산이 없습니다.");
        }

        List<AssetResponseDto> dtoList = filtered.stream().map(asset -> {

            if (asset instanceof AssetLiquid liquid) {
                return AssetResponseDto.builder()
                        .detailId(liquid.getAssetId())
                        .type(liquid.getAssetType())
                        .name(liquid.getAssetName())
                        .amount(liquid.getAmount())
                        .updatedAt(liquid.getUpdateAt())
                        .build();
            }

            if (asset instanceof AssetInvestment invest) {
                Long amount = invest.getAvgPrice()
                        .multiply(invest.getQuantity())
                        .longValue();

                // 🚨 시세 API 붙일 때 여기 계산 추가
                // BigDecimal currentPrice = stockApi.getCurrentPrice(invest.getSymbol());
                // BigDecimal evaluatedAmount = currentPrice.multiply(invest.getQuantity());
                // BigDecimal profitAmount = evaluatedAmount.subtract(invest.getAvgPrice().multiply(invest.getQuantity()));
                // BigDecimal profitRate = profitAmount.divide(originalAmount).multiply(100);

                return AssetResponseDto.builder()
                        .detailId(invest.getAssetId())
                        .type(invest.getAssetType())
                        .name(invest.getAssetName())
                        .amount(amount)
                        .quantity(invest.getQuantity())
                        .avgPrice(invest.getAvgPrice())

                        // 나중에 API 연결하면 아래 추가
                        // .currentPrice(currentPrice)
                        // .evaluatedAmount(evaluatedAmount)
                        // .profitAmount(profitAmount)
                        // .profitRate(profitRate)

                        .updatedAt(invest.getUpdateAt())
                        .build();
            }

            return null;
        }).toList();

        return AssetListResponseDto.builder()
                .assets(dtoList)
                .build();
    }

    //자산 전체요약
    public AssetSummaryResponseDto getAssetSummary(Long userId) {

        List<Asset> assets = assetRepository.findAllByMember_Id(userId);

        if (assets.isEmpty()) {
            throw new AssetNotFoundException("등록된 자산이 없습니다.");
        }

        // 1. 현재 총 자산 합계
        long totalAmount = assets.stream().mapToLong(asset -> {
            if (asset instanceof AssetLiquid liquid) {
                return liquid.getAmount();
            }
            if (asset instanceof AssetInvestment invest) {
                return invest.getAvgPrice().multiply(invest.getQuantity()).longValue();
            }
            return 0L;
        }).sum();

        // 2. 자산 유형별 합
        Map<String, Long> details = assets.stream().collect(Collectors.groupingBy(
                asset -> asset.getAssetType().name(),
                Collectors.summingLong(asset -> {
                    if (asset instanceof AssetLiquid liquid) {
                        return liquid.getAmount();
                    }
                    if (asset instanceof AssetInvestment invest) {
                        return invest.getAvgPrice().multiply(invest.getQuantity()).longValue();
                    }
                    return 0L;
                })
        ));

        // 3. 전일 자산 조회 (없으면 null)
        AssetHistory yesterday = assetHistoryRepository.findTopByMember_IdOrderByRecordedAtDesc(userId);

        long changeAmount = 0;
        double changeRate = 0.0;

        if (yesterday != null && yesterday.getTotalAmount() != 0) {
            changeAmount = totalAmount - yesterday.getTotalAmount();
            changeRate = (changeAmount / (double) yesterday.getTotalAmount()) * 100;
        }

        return AssetSummaryResponseDto.builder()
                .totalAmount(totalAmount)
                .changeAmount(changeAmount)
                .changeRate(changeRate)
                .details(details)
                .build();
    }

    //자산 이력 조회(날짜별 총자산)
    public AssetHistoryResponseDto getAssetHistory(Long userId, LocalDate start, LocalDate end) {

        if (start.isAfter(end)) {
            throw new InvalidDateRangeException("조회 기간이 올바르지 않습니다.");
        }

        List<AssetHistory> records =
                assetHistoryRepository.findAllByMember_IdAndRecordedAtBetween(userId, start, end);

        if (records.isEmpty()) {
            throw new AssetNotFoundException("등록된 자산이 없습니다.");
        }

        List<AssetHistoryItemDto> result = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            AssetHistory current = records.get(i);

            double changeRate = 0.0;

            if (i > 0) {
                AssetHistory prev = records.get(i - 1);
                if (prev.getTotalAmount() != 0) {
                    changeRate =
                            ((current.getTotalAmount() - prev.getTotalAmount()) / (double) prev.getTotalAmount()) * 100;
                }
            }

            result.add(AssetHistoryItemDto.builder()
                    .recordedAt(current.getRecordedAt())
                    .totalAmount(current.getTotalAmount())
                    .changeRate(changeRate)
                    .build());
        }

        return AssetHistoryResponseDto.builder()
                .history(result)
                .build();
    }

    //코인 주식 수정
    public void updateInvestmentAsset(Long userId, Long assetId, UpdateInvestmentAssetDto dto) {

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException("해당 자산을 찾을 수 없습니다."));

        // 본인 자산이 아니면 404 처리
        if (!asset.getMember().getId().equals(userId)) {
            throw new AssetNotFoundException("해당 자산을 찾을 수 없습니다.");
        }

        // 투자자산인지 체크
        if (!(asset instanceof AssetInvestment invest)) {
            throw new InvalidAmountException("투자 자산(STOCK, COIN)만 수정할 수 있습니다.");
        }

        // 값이 들어왔으면 검증 후 업데이트
        if (dto.getQuantity() != null) {
            if (dto.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAmountException("수량 또는 평균 단가는 0보다 커야 합니다.");
            }
            invest.setQuantity(dto.getQuantity());
        }

        if (dto.getAvgPrice() != null) {
            if (dto.getAvgPrice() <= 0) {
                throw new InvalidAmountException("수량 또는 평균 단가는 0보다 커야 합니다.");
            }
            invest.setAvgPrice(BigDecimal.valueOf(dto.getAvgPrice()));
        }

        assetRepository.save(invest);
    }

    //현금, 은행 금액 수정
    public void updateLiquidAsset(Long userId, Long assetId, UpdateLiquidAssetDto dto) {

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException("해당 자산을 찾을 수 없습니다."));

        // 본인 자산 검증
        if (!asset.getMember().getId().equals(userId)) {
            throw new AssetNotFoundException("해당 자산을 찾을 수 없습니다.");
        }

        // Liquid 자산인지 체크
        if (!(asset instanceof AssetLiquid liquid)) {
            throw new InvalidAmountException("현금/은행 자산만 수정할 수 있습니다.");
        }

        // amount 검증
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new InvalidAmountException("금액은 0보다 커야 합니다.");
        }

        liquid.setAmount(dto.getAmount());
        assetRepository.save(liquid);
    }

    //자산 삭제
    public void deleteAsset(Long userId, Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException("해당 자산을 찾을 수 없습니다."));

        if (!asset.getMember().getId().equals(userId)) {
            throw new AssetNotFoundException("해당 자산을 찾을 수 없습니다.");
        }

        assetRepository.delete(asset);
    }

}
