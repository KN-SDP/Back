package com.knusdp.SmartLedger.scheduler;

import com.knusdp.SmartLedger.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시 실행
    @Transactional
    public void cleanupDeletedMembers() {
        LocalDateTime limit = LocalDateTime.now().minusDays(14);

        int affectedRows = memberRepository.deleteByDeletedIsTrueAndDeletedAtBefore(limit);

        log.info("🧹 [Scheduler] 삭제 처리된 회원 수: {}", affectedRows);
    }
}
