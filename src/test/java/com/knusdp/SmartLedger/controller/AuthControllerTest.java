package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.LoginFailedException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.FindInFoService;
import com.knusdp.SmartLedger.service.MemberService;
import com.knusdp.SmartLedger.util.CryptoUtil;
import com.knusdp.SmartLedger.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberService memberService;
    @Autowired private AuthService authService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private FindInFoService findInFoService;
    @Autowired private JwtUtil jwtUtil;

    @MockBean private CryptoUtil cryptoUtil;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setup() {
        when(cryptoUtil.encrypt(anyString())).thenAnswer(invocation -> "encrypted_" + invocation.getArgument(0));
        when(cryptoUtil.decrypt(anyString())).thenAnswer(invocation -> {
            String encryptedValue = invocation.getArgument(0);
            return encryptedValue.startsWith("encrypted_") ? encryptedValue.substring("encrypted_".length()) : encryptedValue;
        });
    }

    @Test
    @DisplayName("로그인 성공 시 토큰에 올바른 정보가 담겨있는지 확인")
    void login_success_and_verify_token_claims() {
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber("encrypted_01012345678")
                .loginType(LoginType.LOCAL) // ★ 필수 추가
                .deleted(false)
                .birth(LocalDate.parse("2000-01-01"))
                .nickname("테스트닉네임")
                .build();
        memberRepository.save(member);

        LoginResponseDto response = authService.login("1111@gmail.com", "123456");

        assertThat(response).isNotNull();
        String token = response.getAccessToken();
        assertThat(token).isNotBlank();

        String userIdFromToken = jwtUtil.getUserIdFromToken(token);
        String usernameFromToken = jwtUtil.getUsernameFromToken(token);

        assertThat(userIdFromToken).isEqualTo(String.valueOf(member.getId()));
        assertThat(usernameFromToken).isEqualTo("jiwoo");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 시 예외 발생")
    void login_fail_wrong_password() {
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber("encrypted_01012345678")
                .loginType(LoginType.LOCAL) // ★ 필수 추가
                .deleted(false)
                .birth(LocalDate.parse("2000-01-01"))
                .nickname("테스트닉네임1")
                .build();
        memberRepository.save(member);

        assertThrows(LoginFailedException.class, () -> authService.login("1111@gmail.com", "wrongpw"));
    }

    @Test
    @DisplayName("회원가입 성공 및 전화번호 암호화 검증")
    void signUp_success() {
        SaveUserLoginInfoDto dto = new SaveUserLoginInfoDto(
                "test@test.com", "abcdef", "abcdef", "jiwoo", "tester", "1999-01-01", "01099998888"
        );

        // 반환 타입을 Member로 수정
        Member savedMember = memberService.saveUserInfo(dto);

        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("test@test.com");
        assertThat(passwordEncoder.matches("abcdef", savedMember.getPassword())).isTrue();
        assertThat(savedMember.getLoginType()).isNotNull(); // loginType 검증 추가
    }

    @Test
    @DisplayName("계정 복구 - 성공")
    void recoverId_success() {
        Member member = Member.builder()
                .username("jiwoo")
                .email("recover@test.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber("encrypted_01011112222")
                .loginType(LoginType.LOCAL) // ★ 필수 추가
                .deleted(false)
                .birth(LocalDate.parse("1995-01-01"))
                .nickname("recoverTest")
                .build();
        memberRepository.save(member);

        String foundEmail = findInFoService.findId("jiwoo", "01011112222", "1995-01-01");

        assertThat(foundEmail).isEqualTo("recover@test.com");
    }

    @Test
    @DisplayName("계정 복구 - 사용자 없을 시 예외 발생")
    void recoverId_userNotFound() {
        assertThrows(UserNotFoundException.class,
                () -> findInFoService.findId("nonexistent", "01000000000", "2000-01-01"));
    }
}
