package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.UserService;

import com.knusdp.SmartLedger.util.CryptoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CryptoUtil cryptoUtil;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @BeforeEach
    void setup() {
        // 테스트 시작 전에 CryptoUtil의 동작을 정의 (Mocking)
        when(cryptoUtil.encrypt(anyString())).thenAnswer(invocation -> {
            // 어떤 문자열이 들어오든 "encrypted_"를 앞에 붙여서 반환
            return "encrypted_" + invocation.getArgument(0);
        });
        when(cryptoUtil.decrypt(anyString())).thenAnswer(invocation -> {
            // "encrypted_"로 시작하는 문자열을 원래대로 돌려줌
            String encryptedValue = invocation.getArgument(0);
            if (encryptedValue.startsWith("encrypted_")) {
                return encryptedValue.substring("encrypted_".length());
            }
            return encryptedValue;
        });
    }

    @Test
    @DisplayName("로그인 성공")


    void login_success() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01012345678")) // 암호화된 폰번호

                .birth(LocalDate.parse("20000101", formatter)) // LocalDate로 변환

                .nickname("테스트닉네임")
                .build();
        userRepository.save(member);

        // when
        LoginResponseDto response = authService.login("1111@gmail.com", "123456");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("1111@gmail.com");
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")

    void login_fail_wrong_password() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01012345678")) // 암호화된 폰번호


                .birth(LocalDate.parse("20000101", formatter))
                .nickname("테스트닉네임1")
                .build();
        userRepository.save(member);

        // when
        LoginResponseDto response = authService.login("1111@gmail.com", "wrongpw");

        // then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("회원가입 성공 및 전화번호 암호화 검증")

    void signUp_success() {
        // given
        SaveUserLoginInfoDto dto = new SaveUserLoginInfoDto();
        dto.setUserName("jiwoo");
        dto.setUserEmail("test@test.com");
        dto.setUserPassword("abcdef");
        dto.setCheckedPassword("abcdef");
        dto.setUserPhoneNumber("01099998888");
        dto.setUserBirth("1999-01-01");
        dto.setUserNickname("tester");

        // when
        Member saved = userService.saveUserInfo(dto);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(passwordEncoder.matches("abcdef", saved.getPassword())).isTrue();
        assertThat(saved.getBirth()).isEqualTo(LocalDate.of(1999, 1, 1));

        // 전화번호 암호화 검증
        String decryptedPhoneNumber = cryptoUtil.decrypt(saved.getPhoneNumber());
        assertThat(decryptedPhoneNumber).isEqualTo(dto.getUserPhoneNumber());

    }
    @Test
    @DisplayName("계정 복구 - 성공")
    void recoverId_success() {
        // 회원 저장
        Member member = Member.builder()
                .username("jiwoo")
                .email("recover@test.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01011112222"))
                .birth(LocalDate.parse("19950101", formatter))
                .nickname("recoverTest")
                .build();
        userRepository.save(member);

        // Service 호출
        Optional<String> emailOpt = authService.findId("jiwoo", "01011112222", "1995-01-01");

        assertThat(emailOpt).isPresent();
        assertThat(emailOpt.get()).isEqualTo("recover@test.com");
    }

    @Test
    @DisplayName("계정 복구 - 사용자 없음")
    void recoverId_userNotFound() {
        Optional<String> emailOpt = authService.findId("nonexistent", "01000000000", "2000-01-01");

        assertThat(emailOpt).isNotPresent();
    }
}

