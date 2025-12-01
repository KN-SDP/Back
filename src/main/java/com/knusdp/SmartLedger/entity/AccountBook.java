package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "AccountBook") // ERD의 테이블 이름과 일치
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId; // bigint -> Long (PK)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount; // DECIMAL(15, 0) -> BigDecimal

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType; // ENUM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType; // ENUM

    @Column(nullable = false)
    private LocalDate transactionDate; // LocalDateTime

    @CreationTimestamp // INSERT 시 자동으로 현재 시간 저장
    @Column(nullable = false, updatable = false)
    private LocalDate date; // ERD의 '작성일' 컬럼, updatable = false

    // Member와의 관계 (userId FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private Member member;

    // Account_Category와의 관계 (categoryId FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId", nullable = false)
    private AccountCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goalId", nullable = true)
    private Goal goal;

}