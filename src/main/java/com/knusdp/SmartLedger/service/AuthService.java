package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.LoginFailedException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import com.knusdp.SmartLedger.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;

    public LoginResponseDto login(String email, String checkedPassword) {
        // 이메일로 사용자를 찾고, 없으면 LoginFailedException을 던집니다.
        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호가 일치하지 않으면 LoginFailedException을 던집니다.
        if (!passwordEncoder.matches(checkedPassword, member.getPassword())) {
            throw new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = JwtUtil.generateToken(String.valueOf(member.getId()));

        return new LoginResponseDto(
                member.getId(),
                member.getEmail(),
                member.getUsername(),
                member.getNickname(),
                token
        );
    }

    public String findId(String username, String phoneNum, String birth) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phoneNum);

        // 사용자를 찾고, 없으면 UserNotFoundException을 던집니다.
        Member member = userRepository.findByUsernameAndPhoneNumberAndBirth(username, encryptedPhone, birthDate)
                .orElseThrow(() -> new UserNotFoundException("일치하는 계정을 찾을 수 없습니다."));

        return member.getEmail();
    }
}