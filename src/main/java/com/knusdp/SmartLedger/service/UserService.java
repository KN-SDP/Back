package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.ErrorResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.EmailDuplicateException;
import com.knusdp.SmartLedger.exception.NickNameDuplicateException;
import com.knusdp.SmartLedger.repository.UserRepository;

import com.knusdp.SmartLedger.util.CryptoUtil;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
@Setter
@Builder
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final CryptoUtil cryptoUtil;

    public Member saveUserInfo(SaveUserLoginInfoDto dto) {
        if (!dto.getUserPassword().equals(dto.getCheckedPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.findByEmail(dto.getUserEmail()).isPresent()) {
            throw new EmailDuplicateException("이미 등록된 이메일입니다.");
        }
        if (userRepository.findByNickname(dto.getUserNickname()).isPresent()) {
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

        return userRepository.save(member);
    }
}
