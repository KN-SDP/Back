package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByMemberOrderByCreatedAtDesc(Member member);
}
