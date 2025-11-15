package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OAuth2LoginSuccessHandlerTest {

    @Autowired
    private OAuth2LoginSuccessHandler successHandler;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private JwtUtil jwtUtil; // 실제 Bean 대신 Mock 처리

    private Member testMember;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 사용자 생성
        testMember = Member.builder()
                .email("oauth@test.com")
                .username("테스트유저")
                .nickname("oauthUser")
                .password("testpass")
                .phoneNumber("000")
                .loginType(LoginType.LOCAL)
                .birth(LocalDate.now())
                .build();
        memberRepository.save(testMember);

        // JWT Mock 설정
        when(jwtUtil.generateToken(testMember)).thenReturn("mockToken");
        when(jwtUtil.validateToken("mockToken")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("mockToken")).thenReturn(String.valueOf(testMember.getId()));
        when(jwtUtil.getEmailFromToken("mockToken")).thenReturn(testMember.getEmail());
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시, JWT 토큰을 포함하여 프론트 URL로 리디렉션")
    void onAuthenticationSuccess_RedirectsWithToken() throws Exception {
        // given
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("id", testMember.getId()),
                "id"
        );

        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).startsWith("https://knusdpsl.mooo.com/oauth-redirect");
        assertThat(redirectUrl).contains("?token=mockToken");
        assertThat(jwtUtil.validateToken("mockToken")).isTrue();
        assertThat(jwtUtil.getUserIdFromToken("mockToken")).isEqualTo(String.valueOf(testMember.getId()));
        assertThat(jwtUtil.getEmailFromToken("mockToken")).isEqualTo(testMember.getEmail());
    }
}
