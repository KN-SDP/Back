package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.UserInFoDto;
import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.*;
import com.knusdp.SmartLedger.exception.auth.AccountDeletedException;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;
    private final JwtUtil jwtUtil;


    // AuthService.java
    public LoginResponseDto login(String email, String checkedPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 🔥 탈퇴 계정 로그인 차단
        if (Boolean.TRUE.equals(member.getDeleted())) {
            throw new AccountDeletedException("탈퇴된 계정입니다. 14일 이내 복구가 가능합니다.");
        }

        if (!passwordEncoder.matches(checkedPassword, member.getPassword())) {
            throw new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(member);

        if (token == null) {
            throw new LoginFailedException("로그인 실패");
        }

        return new LoginResponseDto(token);
    }

    //계정 복구
    @Transactional
    public void restoreAccount(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        if (!Boolean.TRUE.equals(member.getDeleted())) {
            throw new IllegalStateException("이미 복구된 계정입니다.");
        }

        member.setDeleted(false);
        member.setDeletedAt(null);
    }

    //회원 탈퇴 소프트삭제
    @Transactional
    public void withdraw(Long userId, String currentPassword) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        if (member.getDeleted()) {
            throw new AlreadyDeletedException("이미 탈퇴 처리된 계정입니다.");
        }

        if (member.getLoginType() == LoginType.LOCAL) {
            if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
                throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
            }
        }

        member.setDeleted(true);
        member.setDeletedAt(LocalDateTime.now());

    }


}