package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;    // 예산 ID (PK)

    // Member와 다대일 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)  // FK
    private Member member;

    // AccountCategory와 다대일 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)  // FK
    private AccountCategory category;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;   // 예산 금액(15자리 정수)

    @Column(nullable = false)
    private LocalDateTime createdAt; // 설정 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}