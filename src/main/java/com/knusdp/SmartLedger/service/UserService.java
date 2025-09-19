package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.UserRepository;

import com.knusdp.SmartLedger.util.CryptoUtil;

import lombok.*;
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


    public Member saveUserInfo(SaveUserLoginInfoDto dto){
        if (!dto.getUserPassword().equals(dto.getCheckedPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 검사
        if (userRepository.findByEmail(dto.getUserEmail()).isPresent()) {

            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
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
