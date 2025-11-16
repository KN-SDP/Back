package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.EmailDuplicateException;
import com.knusdp.SmartLedger.exception.NickNameDuplicateException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;

import com.knusdp.SmartLedger.util.CryptoUtil;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;

    public Member saveUserInfo(SaveUserLoginInfoDto dto) {
        if (!dto.getUserPassword().equals(dto.getCheckedPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (memberRepository.findByEmail(dto.getUserEmail()).isPresent()) {
            throw new EmailDuplicateException("이미 등록된 이메일입니다.");
        }
        if (memberRepository.findByNickname(dto.getUserNickname()).isPresent()) {
            throw new NickNameDuplicateException("이미 사용중인 닉네임입니다.");
        }
        if (!dto.getUserEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        String encryptedPhoneNumber = cryptoUtil.encrypt(dto.getUserPhoneNumber());

        Member member = Member.builder()
                .username(dto.getUserName())
                .password(passwordEncoder.encode(dto.getUserPassword()))
                .email(dto.getUserEmail())
                .phoneNumber(encryptedPhoneNumber)
                .birth(LocalDate.parse(dto.getUserBirth()))
                .nickname(dto.getUserNickname())
                .build();

        return memberRepository.save(member);
    }


//    // 사용자 정보 확인
//    public boolean validateMember(String email, String username, String birth, String phone) {
//        LocalDate birthDate = LocalDate.parse(birth);
//
//        // 평문을 암호화해서 비교
//        String encryptedPhone = cryptoUtil.encrypt(phone);
//
//        return memberRepository.findByEmailAndUsernameAndBirthAndPhoneNumber(
//                email, username, birthDate, encryptedPhone
//        ).isPresent();
//    }


    // 비밀번호 재설정
    public String issueResetToken(String email, String username, String birth, String phone) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phone);

        Optional<Member> optionalMember = memberRepository.findByEmailAndUsernameAndBirthAndPhoneNumber(
                email, username, birthDate, encryptedPhone
        );

        if (optionalMember.isEmpty()) return null;

        Member member = optionalMember.get();
        String token = UUID.randomUUID().toString();
        member.setResetToken(token);
        // 10분 지난 토큰 무효처리
        member.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
        memberRepository.save(member);

        return token;
    }

    public boolean resetPasswordByToken(String token, String newPassword, String checkedPassword) {
        if (!newPassword.equals(checkedPassword))
            throw new IllegalArgumentException("PasswordMismatch");

        Optional<Member> optionalMember = memberRepository.findByResetToken(token);
        if (optionalMember.isEmpty()) return false;

        Member member = optionalMember.get();

        if (member.getResetTokenExpiry() == null || member.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false; // 만료된 토큰
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        member.setResetToken(null);
        memberRepository.save(member);
        return true;
    }

    public void updateNickname(Long userId, String newNickname) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        memberRepository.findByNickname(newNickname)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(member.getId())) {
                        throw new NickNameDuplicateException("이미 사용중인 닉네임입니다.");
                    }
                });

        member.setNickname(newNickname);
    }
    public Member findOrCreateSocialUser(String provider, String providerId, String email, String name) {

        // 1. providerId로 사용자를 먼저 찾습니다.
        Optional<Member> memberOpt = memberRepository.findByProviderId(providerId);
        if (memberOpt.isPresent()) {
            return memberOpt.get(); // 이미 소셜 로그인으로 가입된 회원이면 반환
        }

        // 2. providerId로는 못찾았지만, 이메일로 가입된 계정이 있는지 확인합니다.
        Optional<Member> emailMemberOpt = memberRepository.findByEmail(email);
        if (emailMemberOpt.isPresent()) {
            // 이미 로컬이나 다른 소셜로 가입된 계정이 있다면,
            // 해당 계정에 소셜 로그인 정보를 연결(업데이트)합니다.
            Member existingMember = emailMemberOpt.get();
            existingMember.setProviderId(providerId);
            existingMember.setLoginType(LoginType.valueOf(provider.toUpperCase())); // "google" -> LoginType.GOOGLE
            return memberRepository.save(existingMember); // 업데이트 후 반환
        }

        // 3. 완전히 새로운 사용자입니다. 새로 가입시킵니다.
        Member newMember = Member.builder()
                .email(email)
                .username(name)
                // 닉네임은 중복될 수 있으므로 임시값 처리 (예: "Google_12345")
                .nickname(provider + "_" + providerId.substring(0, 6))
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 임시 비밀번호
                .birth(LocalDate.of(1900, 1, 1)) // 임시 생년월일
                .phoneNumber(cryptoUtil.encrypt(providerId)) // 임시 전화번호 (고유해야 함)
                .loginType(LoginType.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .build();

        Member savedMember = memberRepository.save(newMember);

        return savedMember;
    }


    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }
}
