package com.knusdp.SmartLedger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false, unique = true, length = 50)
    private String username;  // 유저 ID


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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}