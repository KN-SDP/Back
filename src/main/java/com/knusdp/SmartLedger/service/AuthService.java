package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.FindIdResponseDto;
import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import com.knusdp.SmartLedger.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;

    public LoginResponseDto login(String email, String checkedPassword) {
        Optional<Member> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Member member = userOpt.get();
            System.out.println("DB PW: " + member.getPassword());
            System.out.println("입력 PW: " + checkedPassword);

            if (passwordEncoder.matches(checkedPassword, member.getPassword())) {
                String token = JwtUtil.generateToken(String.valueOf(member.getId()));

                return new LoginResponseDto(
                        member.getId(),
                        member.getEmail(),
                        member.getUsername(),
                        member.getNickname(),
                        token
                );
            }
        }
        return null;
    }
    public Optional<String> findId(String username, String phoneNum, String birth) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phoneNum);
        return userRepository.findByUsernameAndPhoneNumberAndBirth(username, encryptedPhone, birthDate)
                .map(Member::getEmail);
    }
}
