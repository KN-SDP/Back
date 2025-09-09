package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.entity.User;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("DB PW: " + user.getPassword());
            System.out.println("입력 PW: " + rawPassword);

            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                String token = JwtUtil.generateToken(String.valueOf(user.getId()));

                return new LoginResponseDto(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getNickname(),
                        token
                );
            }
        }
        return null;
    }
}
