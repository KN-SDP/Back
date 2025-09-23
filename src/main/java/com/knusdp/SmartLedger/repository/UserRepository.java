package com.knusdp.SmartLedger.repository;

import com.knusdp.SmartLedger.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUsernameAndPhoneNumberAndBirth(String username, String phoneNumber, LocalDate birth);


}
