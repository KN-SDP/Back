package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.UserRepository;
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
    public Member saveUserInfo(SaveUserLoginInfoDto dto){
        Member member = Member.builder()
                .username(dto.getUserName())
                .password(passwordEncoder.encode(dto.getUserPassword()))
                .email(dto.getUserEmail())
                .phoneNumber(dto.getUserPhoneNumber())
                .birth(LocalDate.parse(dto.getUserBirth()))
                .nickname(dto.getUserNickname())
                .build();

        return userRepository.save(member);
    }
}
