package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBook, Long>, JpaSpecificationExecutor<AccountBook> {
    @Query("SELECT ab FROM AccountBook ab " +
            "WHERE ab.member.id = :memberId " +
            "AND ab.transactionType = :transactionType " +
            "AND ab.category.categoryName = :categoryName")
    List<AccountBook> findByMemberAndTransactionTypeAndCategoryName(
            @Param("memberId") Long memberId,
            @Param("transactionType") TransactionType transactionType,
            @Param("categoryName") String categoryName
    );

    List<AccountBook> findByMemberIdAndTransactionType(Long memberId, TransactionType transactionType);

    @Query("SELECT ab FROM AccountBook ab " +
            "WHERE ab.member.id = :memberId " +
            "AND YEAR(ab.transactionDate) = :year " +
            "AND MONTH(ab.transactionDate) = :month")
    List<AccountBook> findEntriesByYearAndMonth(
            @Param("memberId") Long memberId,
            @Param("year") int year,
            @Param("month") int month
    );
    Optional<AccountBook> findByMemberIdAndTransactionId(Long memberId, Long transactionId);
}
