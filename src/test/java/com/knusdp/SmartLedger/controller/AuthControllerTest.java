package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginRequestDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.User;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    void login_success() {
        // given
        User user = User.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456")) // 인코딩 필요
                .phoneNumber("01012345678")
                .birth("20000101")
                .build();
        userRepository.save(user);

        // when
        LoginRequestDto response = authService.login("1111@gmail.com", "123456");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserEmail()).isEqualTo("1111@gmail.com");
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void login_fail_wrong_password() {
        // given
        User user = User.builder()
                .username("jiwoo")
                .email("1111@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .phoneNumber("01012345678")
                .birth("20000101")
                .build();
        userRepository.save(user);

        // when
        LoginRequestDto response = authService.login("1111@gmail.com", "wrongpw");

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
        dto.setUserBirth("19990101");

        // when
        User saved = userService.saveUserInfo(dto);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(passwordEncoder.matches("abcdef", saved.getPassword())).isTrue();
    }
}
