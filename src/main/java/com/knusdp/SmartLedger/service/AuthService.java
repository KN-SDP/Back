package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.UserInFoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.LoginFailedException;
import com.knusdp.SmartLedger.exception.NickNameDuplicateException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        if (!passwordEncoder.matches(checkedPassword, member.getPassword())) {
            throw new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        String token = jwtUtil.generateToken(member);

        return new LoginResponseDto(token);
    }
    @Transactional
    public void changeNickname(String nickname, String token){
        Long userId = Long.parseLong(jwtUtil.getUserIdFromToken(token));
        Optional<Member> foundByNickname = memberRepository.findByNickname(nickname);
        if (foundByNickname.isPresent() && !foundByNickname.get().getId().equals(userId))
        {
            throw new NickNameDuplicateException("이미 사용중인 닉네임입니다.");
        }
        else {
            Member member = memberRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));
            member.setNickname(nickname);
        }
    }
}