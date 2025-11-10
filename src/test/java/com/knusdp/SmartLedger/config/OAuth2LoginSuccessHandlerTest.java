package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // test/resources/application-test.yml 사용
class OAuth2LoginSuccessHandlerTest {

    @Autowired
    private OAuth2LoginSuccessHandler successHandler;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil; // 실제 JwtUtil Bean 주입

    private Member testMember;

    @BeforeEach
    void setup() {
        // 테스트용 사용자 생성
        testMember = Member.builder()
                .email("oauth@test.com")
                .username("테스트유저")
                .nickname("oauthUser")
                .password("testpass")
                .phoneNumber("000")
                .birth(LocalDate.now())
                .build();
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시, JWT 토큰을 포함하여 프론트 URL로 리디렉션")
    void onAuthenticationSuccess_RedirectsWithToken() throws Exception {
        // given
        // 1. Mock 객체 생성
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = mock(Authentication.class);

        // 2. CustomOAuth2UserService가 반환했을 Mock Principal 생성
        //    oAuth2User.getName()이 memberId를 반환하도록 "id"를 키로 사용
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("id", testMember.getId()), // "id"가 Principal의 이름이 됨
                "id"
        );

        // 3. Authentication 객체가 Mock Principal을 반환하도록 설정
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // 4. Redirect URL을 캡처하기 위한 ArgumentCaptor 생성

        // when
        // 5. 핸들러 실행
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        // 6. response.sendRedirect()가 호출되었는지, 그리고 그 URL이 무엇인지 캡처
        String redirectUrl = response.getRedirectedUrl();
        // 7. URL 검증
        assertThat(redirectUrl).startsWith("https://knusdpsl.mooo.com/oauth-redirect");
        assertThat(redirectUrl).contains("?token=");

        // 8. 토큰 검증
        String token = redirectUrl.substring(redirectUrl.indexOf("token=") + 6);
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(String.valueOf(testMember.getId()));
        assertThat(jwtUtil.getEmailFromToken(token)).isEqualTo(testMember.getEmail());
    }
}