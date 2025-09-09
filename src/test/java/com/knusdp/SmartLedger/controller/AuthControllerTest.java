package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

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

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    void login_success() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber("01012345678")
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
    void login_fail_wrong_password() {
        // given
        Member member = Member.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber("01012345678")
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
    }

}
