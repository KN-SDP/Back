package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.TransactionType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class AccountBookSpecification {

    // 기본 조건: 현재 로그인한 사용자의 데이터만 조회
    public static Specification<AccountBook> hasMemberId(Long memberId) {
        return (root, query, cb) -> cb.equal(root.get("member").get("id"), memberId);
    }

    // 조건 1: 연도(year)가 일치하는지
    public static Specification<AccountBook> hasYear(int year) {
        return (root, query, cb) -> cb.equal(
                cb.function("YEAR", Integer.class, root.get("transactionDate")), year
        );
    }

    // 조건 2: 월(month)이 일치하는지
    public static Specification<AccountBook> hasMonth(int month) {
        return (root, query, cb) -> cb.equal(
                cb.function("MONTH", Integer.class, root.get("transactionDate")), month
        );
    }

    public static Specification<AccountBook> hasExactDate(int year, int month, int day) {
        return (root, query, cb) -> {
            LocalDate date = LocalDate.of(year, month, day);

            return cb.between(
                    root.get("transactionDate"),
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay()
            );
        };
    }



    // 조건 3: 거래 타입(transactionType)이 일치하는지
    public static Specification<AccountBook> hasTransactionType(TransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("transactionType"), type);
    }

    // 조건 4: 카테고리 이름(categoryName)이 일치하는지 (Join 필요)
    public static Specification<AccountBook> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            Join<AccountBook, AccountCategory> categoryJoin = root.join("category");
            return cb.equal(categoryJoin.get("categoryName"), categoryName);
        };
    }
}