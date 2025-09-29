package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.FindInFoService;
import com.knusdp.SmartLedger.service.MemberService;
import com.knusdp.SmartLedger.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FindInFoService findInFoService;

    @MockBean
    private CryptoUtil cryptoUtil;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    // AuthControllerTest.java

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01012345678"))
                .birth(LocalDate.parse("2000-01-01", formatter))
                .nickname("테스트닉네임")
                .build();
        memberRepository.save(member);

        // when
        LoginResponseDto response = authService.login("1111@gmail.com", "123456");

        // then
        // 이제 DTO가 null이 아니고, accessToken 필드가 비어있지 않은지만 확인합니다.
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_fail_wrong_password() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01012345678"))
                .birth(LocalDate.parse("2000-01-01", formatter))
                .nickname("테스트닉네임1")
                .build();
        memberRepository.save(member);

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
        Member saved = memberService.saveUserInfo(dto);

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
                .birth(LocalDate.parse("1995-01-01", formatter))
                .nickname("recoverTest")
                .build();
        memberRepository.save(member);

        // Service 호출
        Optional<String> emailOpt = findInFoService.findId("jiwoo", "01011112222", "1995-01-01");

        assertThat(emailOpt).isPresent();
        assertThat(emailOpt.get()).isEqualTo("recover@test.com");
    }

    @Test
    @DisplayName("계정 복구 - 사용자 없음")
    void recoverId_userNotFound() {
        Optional<String> emailOpt = findInFoService.findId("nonexistent", "01000000000", "2000-01-01");

        assertThat(emailOpt).isNotPresent();
    }

    /*비밀번호 찾기*/
    @Test
    @DisplayName("비밀번호 재설정 - 성공")
    void resetPassword_success() {
        // given (회원 가입)
        Member member = Member.builder()
                .username("jiwoo")
                .email("reset@test.com")
                .password(passwordEncoder.encode("oldpassword"))
                .phoneNumber(cryptoUtil.encrypt("01022223333"))
                .birth(LocalDate.parse("1998-01-01", formatter))
                .nickname("resetTester")
                .build();
        memberRepository.save(member);

        // when (비밀번호 재설정 시도)
        boolean result = memberService.resetPassword("reset@test.com", "newpassword", "newpassword");

        // then
        assertThat(result).isTrue();

        // 비밀번호가 실제로 변경되었는지 확인
        Member updated = memberRepository.findByEmail("reset@test.com").get();
        assertThat(passwordEncoder.matches("newpassword", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 재설정 - 비밀번호 확인 불일치")
    void resetPassword_passwordMismatch() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("mismatch@test.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01044445555"))
                .birth(LocalDate.parse("1997-01-01", formatter))
                .nickname("mismatchTester")
                .build();
        memberRepository.save(member);

        // when & then (예외 발생 확인)
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            memberService.resetPassword("mismatch@test.com", "newpass", "different");
        });
    }

    @Test
    @DisplayName("비밀번호 재설정 - 사용자 없음")
    void resetPassword_userNotFound() {
        // when
        boolean result = memberService.resetPassword("nouser@test.com", "newpass", "newpass");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("계정 검증 - 성공")
    void validateMember_success() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("validate@test.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01066667777")) // 암호화 저장
                .birth(LocalDate.parse("1996-01-01", formatter))
                .nickname("validateTester")
                .build();
        memberRepository.save(member);

        // when
        // 👉 평문으로 넣어도 Service 내부에서 encrypt() 해서 비교
        boolean valid = memberService.validateMember(
                "validate@test.com", "jiwoo", "1996-01-01", "01066667777"
        );

        // then
        assertThat(valid).isTrue();
    }


    @Test
    @DisplayName("계정 검증 - 실패 (정보 불일치)")
    void validateMember_fail() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("validatefail@test.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01088889999"))
                .birth(LocalDate.parse("1994-01-01", formatter))
                .nickname("failTester")
                .build();
        memberRepository.save(member);

        // when
        boolean valid = memberService.validateMember(
                "validatefail@test.com", "wrongname", "1994-01-01", "01088889999"
        );

        // then
        assertThat(valid).isFalse();
    }


}