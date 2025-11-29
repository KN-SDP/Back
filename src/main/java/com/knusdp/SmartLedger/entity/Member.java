package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter // 개발과정에서만 사용
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK
    
    @Column(length = 50)
    private String username;  // 유저 이름

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String password;  // 비밀번호 (암호화 필수)

    @Column(nullable = false, unique = true, length = 100)
    private String email;     // 이메일

    @Column(nullable = false, unique = true, length = 255)
    private String phoneNumber; //전화번호 (암호화)

    @Column(nullable = false)
    private LocalDate birth; // 생년월일

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;


    @Column(name = "deleted_at",nullable = true)
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) // 1. Enum 이름을 문자열("LOCAL", "GOOGLE")로 저장
    @Column(nullable = false)    // 2. 이 값은 필수이므로 NOT NULL
    private LoginType loginType;
    @Column(unique = true)       // 3. providerId는 고유해야 함 (단, null은 허용)
    private String providerId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    //연관관계의 주인
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Goal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBook> accountBooks = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asset> assets = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetHistory> assetHistory = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordHistory> passwordHistory = new ArrayList<>();

}