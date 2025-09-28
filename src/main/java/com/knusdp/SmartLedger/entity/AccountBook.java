package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(precision = 15, scale = 0, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Column(nullable = false)
    private LocalDate transactionDate;

    // Member와의 관계 (ERD의 userId FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    // Account_Category와의 관계 (ERD의 categoryId FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId", nullable = false)
    private AccountCategory category;

    // ERD의 'date'(작성일) 컬럼을 자동 생성/업데이트 타임스탬프로 관리
    @CreationTimestamp
    @Column(name = "createAt", nullable = false, updatable = false)
    private LocalDate createdAt;
}