package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.dto.UpdateProfileRequestDto;
import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.entity.PasswordHistory;
import com.knusdp.SmartLedger.exception.*;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.repository.PasswordHistoryRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import com.knusdp.SmartLedger.util.CryptoUtil;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;
    private final JwtUtil jwtUtil;

    public Member saveUserInfo(SaveUserLoginInfoDto dto) {

        if (!dto.getUserPassword().equals(dto.getCheckedPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        if (!dto.getUserEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new EmailValidationError("올바른 이메일 형식이 아닙니다.");
        }

        if (memberRepository.findByEmail(dto.getUserEmail()).isPresent()) {
            throw new EmailDuplicateException("이미 등록된 이메일입니다.");
        }

        if (memberRepository.findByNickname(dto.getUserNickname()).isPresent()) {
            throw new NickNameDuplicateException("이미 사용중인 닉네임입니다.");
        }

        LocalDate birth;
        try {
            birth = LocalDate.parse(dto.getUserBirth());
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("생년월일 형식은 YYYY-MM-DD이어야 합니다.");
        }

        String encryptedPhoneNumber = cryptoUtil.encrypt(dto.getUserPhoneNumber());

        Member member = Member.builder()
                .username(dto.getUserName())
                .password(passwordEncoder.encode(dto.getUserPassword()))
                .email(dto.getUserEmail())
                .phoneNumber(encryptedPhoneNumber)
                .birth(birth)
                .loginType(LoginType.LOCAL)
                .nickname(dto.getUserNickname())
                .build();

        return memberRepository.save(member);
    }

    public String issueResetToken(String email, String username, String birth, String phone) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phone);

        Member member = memberRepository
                .findByEmailAndUsernameAndBirthAndPhoneNumber(email, username, birthDate, encryptedPhone)
                .orElseThrow(() ->
                        new UserNotFoundException("입력한 정보와 일치하는 사용자가 없습니다.")
                );

        return jwtUtil.generateResetToken(member.getId(), member.getEmail());
    }


    // 로그인 안했을 때 비번 변경
    public void resetPasswordByToken(String token, String newPassword, String checkedPassword) {

        if (!jwtUtil.validateResetToken(token)) {
            throw new InvalidTokenException("유효하지 않거나 만료된 토큰입니다.");
        }

        Long userId = Long.valueOf(jwtUtil.getUserIdFromToken(token));
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        validateAndUpdatePassword(member, newPassword, checkedPassword);
    }
    // 로그인 했을 때 비번 변경
    public void changePassword(Long userId, String currentPassword, String newPassword, String checkedPassword) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 올바르지 않습니다.");
        }

        validateAndUpdatePassword(member, newPassword, checkedPassword);
    }


    //비번변경 로직
    private void validateAndUpdatePassword(Member member, String newPassword, String checkedPassword) {

        if (!newPassword.equals(checkedPassword)) {
            throw new PasswordMismatchException("비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new SamePasswordException("현재 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }

        List<PasswordHistory> historyList =
                passwordHistoryRepository.findTop5ByMemberOrderByCreatedAtDesc(member);

        for (PasswordHistory history : historyList) {
            if (passwordEncoder.matches(newPassword, history.getPassword())) {
                throw new SamePasswordException("최근 사용한 비밀번호는 다시 사용할 수 없습니다.");
            }
        }

        passwordHistoryRepository.save(
                PasswordHistory.builder()
                        .member(member)
                        .password(member.getPassword())
                        .build()
        );

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }






    @Transactional
    public String updateNickname(Long userId, String newNickname) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        memberRepository.findByNickname(newNickname)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(member.getId())) {
                        throw new NickNameDuplicateException("이미 사용중인 닉네임입니다.");
                    }
                });

        member.setNickname(newNickname);
        return member.getNickname(); // 변경된 닉네임 반환
    }

    public Member findOrCreateSocialUser(String provider, String providerId, String email, String name) {

        Optional<Member> memberOpt = memberRepository.findByProviderId(providerId);
        if (memberOpt.isPresent()) {
            return memberOpt.get(); // 이미 가입된 소셜 회원이면 반환
        }

        Optional<Member> emailMemberOpt = memberRepository.findByEmail(email);
        if (emailMemberOpt.isPresent()) {
            Member existingMember = emailMemberOpt.get();
            existingMember.setProviderId(providerId);
            existingMember.setLoginType(LoginType.valueOf(provider.toUpperCase()));
            return memberRepository.save(existingMember);
        }

        // --- 신규 회원 생성 로직 ---
        Member newMember = Member.builder()
                .email(email)
                .username(name)
                .loginType(LoginType.valueOf(provider.toUpperCase()))
                .providerId(providerId)

                // --- DB 필수값을 채우기 위한 임시 정보 (Dummy Data) ---
                .nickname(provider + "_" + providerId.substring(0, 6)) // UNIQUE 임시 닉네임
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 임시 비밀번호

                .birth(LocalDate.of(1900, 1, 1)) // ★★★ "신규 유저" 꼬리표가 될 임시 생년월일

                // phoneNumber는 UNIQUE이므로, 고유값인 providerId를 암호화하여 임시 저장
                .phoneNumber(cryptoUtil.encrypt(providerId))

                .build();

        return memberRepository.save(newMember);
        // (참고: 기본 카테고리 생성 로직은 DB에 수동 추가하셨으므로 여기서 호출하지 않습니다.)
    }
    public void updateProfile(Long userId, UpdateProfileRequestDto dto) {
        // 1. 닉네임 중복 검사 (본인 제외)
        memberRepository.findByNickname(dto.getNickname())
                .ifPresent(member -> {
                    if (!member.getId().equals(userId)) {
                        throw new NickNameDuplicateException("이미 사용 중인 닉네임입니다.");
                    }
                });

        // 2. 전화번호 중복 검사 (본인 제외)
        String encryptedPhone = cryptoUtil.encrypt(dto.getPhoneNumber());
        memberRepository.findByPhoneNumber(encryptedPhone)
                .ifPresent(member -> {
                    if (!member.getId().equals(userId)) {
                        throw new RuntimeException("이미 등록된 전화번호입니다."); // (PhoneNumberDuplicateException)
                    }
                });

        // 3. 사용자 정보 조회 및 업데이트
        Member memberToUpdate = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        memberToUpdate.setNickname(dto.getNickname());
        memberToUpdate.setBirth(dto.getBirth());
        memberToUpdate.setPhoneNumber(encryptedPhone);

        // @Transactional에 의해 자동 저장 (Dirty Checking)
    }


    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }
}
