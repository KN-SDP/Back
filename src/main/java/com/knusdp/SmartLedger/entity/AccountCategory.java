package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Account_Category") // ERD의 테이블 이름과 일치
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, length = 20)
    private String categoryName;

    // Member와의 관계 (userId FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId") // nullable = true (공용 카테고리 가능성)
    private Member member;
}