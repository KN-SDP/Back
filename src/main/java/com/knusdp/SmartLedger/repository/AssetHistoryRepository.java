package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {
    List<AssetHistory> findByMemberIdOrderByRecordedAtDesc(Long userId);
    AssetHistory findTopByMember_IdOrderByRecordedAtDesc(Long userId);
    List<AssetHistory> findAllByMember_IdAndRecordedAtBetween(Long userId, LocalDate start, LocalDate end);


}
