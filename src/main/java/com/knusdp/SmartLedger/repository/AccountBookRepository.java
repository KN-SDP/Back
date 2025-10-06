package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {
    @Query("SELECT ab FROM AccountBook ab " +
            "WHERE ab.member.id = :memberId " +
            "AND ab.category.categoryName = :categoryName")
    List<AccountBook> findByMemberAndCategoryName(
            @Param("memberId") Long memberId,
            @Param("categoryName") String categoryName
    );
    List<AccountBook> findByMemberIdAndTransactionType(Long memberId, TransactionType transactionType);
}
