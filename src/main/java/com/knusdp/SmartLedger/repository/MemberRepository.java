package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findByUsernameAndPhoneNumberAndBirth(String username, String phoneNumber, LocalDate birth);
    Optional<Member> findByEmailAndUsernameAndBirthAndPhoneNumber(
            String email, String username, LocalDate birth, String phoneNumber

    );
    Optional<Member> findByResetToken(String resetToken);
    Optional<Member> findByProviderId(String providerId);
}
