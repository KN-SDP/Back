package com.knusdp.SmartLedger.scheduler;

import com.knusdp.SmartLedger.entity.AssetHistory;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.AssetHistoryRepository;
import com.knusdp.SmartLedger.repository.AssetRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetHistoryScheduler {

    private final MemberRepository memberRepository;
    private final AssetRepository assetRepository;
    private final AssetHistoryRepository assetHistoryRepository;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 00:00 실행
    @Transactional
    public void recordDailyAssets() {

        log.info("=== Asset Daily Summary Scheduler Started ===");

        // 전체 회원 조회
        memberRepository.findAll().forEach(member -> {

            long totalAmount = assetRepository.findAllByMember_Id(member.getId()).stream()
                    .mapToLong(asset -> {
                        if (asset instanceof com.knusdp.SmartLedger.entity.AssetLiquid liquid) {
                            return liquid.getAmount();
                        }
                        if (asset instanceof com.knusdp.SmartLedger.entity.AssetInvestment invest) {
                            return invest.getAvgPrice().multiply(invest.getQuantity()).longValue();
                        }
                        return 0L;
                    }).sum();

            // 저장
            AssetHistory history = AssetHistory.builder()
                    .member(member)
                    .totalAmount(totalAmount)
                    .recordedAt(LocalDate.now())
                    .build();

            assetHistoryRepository.save(history);
        });

        log.info("=== Asset Daily Summary Completed ===");
    }
}
