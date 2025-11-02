package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByMember_IdOrderByCreatedAtDesc(Long userId);
    Optional<Goal> findByMember_IdAndGoalId(Long userId, Long goalId);
}