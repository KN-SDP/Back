package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CryptoUtil cryptoUtil; // CryptoUtil Mocking

    @BeforeEach
    void setup() {
        // CryptoUtil의 encrypt가 호출되면 "encrypted_" + 원본 문자열을 반환하도록 설정
        when(cryptoUtil.encrypt(anyString())).thenAnswer(invocation -> "encrypted_" + invocation.getArgument(0));
    }

    @Test
    @DisplayName("소셜 로그인 - 1: 신규 사용자일 경우 회원가입")
    void findOrCreateSocialUser_NewUser() {
        // given
        String provider = "google";
        String providerId = "123456789";
        String email = "google_new@test.com";
        String name = "구글유저";

        // when
        Member member = memberService.findOrCreateSocialUser(provider, providerId, email, name);

        // then
        assertThat(member).isNull();
    }

    @Test
    @DisplayName("소셜 로그인 - 2: ProviderId로 기존 회원 조회")
    void findOrCreateSocialUser_FindByProviderId() {
        // given
        // 이미 가입된 소셜 로그인 유저를 미리 저장
        Member existingMember = Member.builder()
                .email("google_old@test.com")
                .providerId("google_98765")
                .loginType(LoginType.GOOGLE)
                .password("testpass")
                .phoneNumber("000")
                .birth(LocalDate.now())
                .nickname("old_google_user")
                .username("기존유저")
                .build();
        memberRepository.save(existingMember);

        // when
        // 같은 providerId로 로그인 시도
        Member member = memberService.findOrCreateSocialUser("google", "google_98765", "google_old@test.com", "기존유저");

        // then
        assertThat(member.getId()).isEqualTo(existingMember.getId());
        assertThat(member.getProviderId()).isEqualTo("google_98765");
    }

    @Test
    @DisplayName("소셜 로그인 - 3: 이메일로 기존 로컬 회원 연동")
    void findOrCreateSocialUser_LinkToLocalUser() {
        // given
        // 이미 가입된 로컬 유저를 미리 저장 (providerId가 null)
        Member localMember = Member.builder()
                .email("local@test.com")
                .loginType(LoginType.LOCAL)
                .password(passwordEncoder.encode("localpass"))
                .phoneNumber("01012345678")
                .birth(LocalDate.now())
                .nickname("로컬유저")
                .username("김로컬")
                .build();
        memberRepository.save(localMember);

        // when
        // 같은 이메일, 새로운 providerId로 로그인 시도
        Member member = memberService.findOrCreateSocialUser("google", "google_new_id", "local@test.com", "김로컬");

        // then
        // 기존 로컬 회원의 ID와 같아야 함
        assertThat(member.getId()).isEqualTo(localMember.getId());
        // providerId와 loginType이 구글 정보로 업데이트되었는지 확인
        assertThat(member.getProviderId()).isEqualTo("google_new_id");
        assertThat(member.getLoginType()).isEqualTo(LoginType.GOOGLE);
    }
}