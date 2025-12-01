package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Goal") // ERD의 테이블 이름과 일치
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId; // 목표 ID (PK)

    @Column(nullable = false, length = 30)
    private String title; // 목표 제목

    @Column(columnDefinition = "TEXT") // 이미지 Url 선택사항
    private String imageUrl;

    @Column(nullable = false, precision = 15, scale = 0) // 목표 금액은 소수점 없다고 가정
    private BigDecimal targetAmount; // 목표 금액

    @Builder.Default // 기본값을 0으로 설정
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal currentAmount = BigDecimal.ZERO; // 현재 달성 금액

    @Column(nullable = false)
    private LocalDate startDate; // 시작일

    @Column(nullable = false)
    private LocalDate deadline; // 마감일

    @Builder.Default // 기본 상태 설정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ONGOING; // 목표 상태 Enum (예시)

    @CreationTimestamp // INSERT 시 자동으로 현재 시간 저장
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일

    @UpdateTimestamp // <-- 수정 시 자동으로 현재 시간 저장
    @Column(nullable = false)
    private LocalDateTime updatedAt; // <-- updatedAt 필드 추가
    // 여러 목표는 하나의 회원에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private Member member; // 목표를 설정한 회원

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBook> accountbook = new ArrayList<>();
}
