package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.LoginFailedException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.FindInFoService;
import com.knusdp.SmartLedger.service.MemberService;
import com.knusdp.SmartLedger.util.CryptoUtil;
import com.knusdp.SmartLedger.util.JwtUtil; // JwtUtil import
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(cryptoUtil.encrypt(anyString())).thenAnswer(invocation -> "encrypted_" + invocation.getArgument(0));
        when(cryptoUtil.decrypt(anyString())).thenAnswer(invocation -> {
            String encryptedValue = invocation.getArgument(0);
            return encryptedValue.startsWith("encrypted_") ? encryptedValue.substring("encrypted_".length()) : encryptedValue;
        });
    }

    @Test
    @DisplayName("로그인 성공 시 토큰에 올바른 정보가 담겨있는지 확인")
    void login_success_and_verify_token_claims() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01012345678"))
                .birth(LocalDate.parse("2000-01-01"))
                .nickname("테스트닉네임")
                .build();
        memberRepository.save(member);

        // when
        LoginResponseDto response = authService.login("1111@gmail.com", "123456");

        // then
        assertThat(response).isNotNull();
        String token = response.getAccessToken();
        assertThat(token).isNotBlank();

        // ★★★ 검증 로직 수정 ★★★
        String userIdFromToken = JwtUtil.getUserIdFromToken(token); // <-- getUserIdFromToken으로 수정
        String usernameFromToken = JwtUtil.getUsernameFromToken(token);

        assertThat(userIdFromToken).isEqualTo(String.valueOf(member.getId())); // ID는 ID와 비교
        assertThat(usernameFromToken).isEqualTo("jiwoo");                    // username은 username과 비교
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 시 예외 발생")
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

        // when & then
        // authService.login 호출 시 LoginFailedException이 발생하는지 검증
        assertThrows(LoginFailedException.class, () -> {
            authService.login("1111@gmail.com", "wrongpw");
        });
    }

    // (회원가입 테스트는 변경 없음)
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
        String decryptedPhoneNumber = cryptoUtil.decrypt(saved.getPhoneNumber());
        assertThat(decryptedPhoneNumber).isEqualTo(dto.getUserPhoneNumber());
    }


    @Test
    @DisplayName("계정 복구 - 성공")
    void recoverId_success() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("recover@test.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber(cryptoUtil.encrypt("01011112222"))
                .birth(LocalDate.parse("1995-01-01", formatter))
                .nickname("recoverTest")
                .build();
        memberRepository.save(member);

        // when
        String foundEmail = findInFoService.findId("jiwoo", "01011112222", "1995-01-01");

        // then
        assertThat(foundEmail).isEqualTo("recover@test.com");
    }

    @Test
    @DisplayName("계정 복구 - 사용자 없을 시 예외 발생")
    void recoverId_userNotFound() {
        // when & then
        // findId 호출 시 UserNotFoundException이 발생하는지 검증
        assertThrows(UserNotFoundException.class, () -> {
            findInFoService.findId("nonexistent", "01000000000", "2000-01-01");
        });
    }

    // (이하 비밀번호 재설정 및 계정 검증 테스트는 기존 로직을 유지하거나,
    //  예외 처리 방식으로 통일하는 것을 고려해볼 수 있습니다.)
}