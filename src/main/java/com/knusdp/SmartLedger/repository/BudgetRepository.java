package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.Budget;
import com.knusdp.SmartLedger.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // (userId, categoryId) unique 체크
    Optional<Budget> findByMemberAndCategory(Member member, AccountCategory category);

    boolean existsByMemberAndCategory(Member member, AccountCategory category);

    List<Budget> findAllByMember(Member member);

}
