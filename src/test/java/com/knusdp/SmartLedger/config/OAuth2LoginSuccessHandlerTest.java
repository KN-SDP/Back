package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify; // verify import

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OAuth2LoginSuccessHandlerTest {

    @Autowired
    private OAuth2LoginSuccessHandler successHandler;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private JwtUtil jwtUtil; // Mock 처리

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
                .phoneNumber("000") // NOT NULL 필드 채우기
                .loginType(LoginType.LOCAL) // NOT NULL 필드 채우기
                .birth(LocalDate.now()) // NOT NULL 필드 채우기
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

        // --- 👇 여기가 수정되었습니다. 👇 ---
        // 2. CustomOAuth2UserService가 반환할 속성 맵을 만듭니다.
        //    "id"와 "member" 객체 자체를 포함시킵니다.
        Map<String, Object> mockAttributes = Map.of(
                "id", testMember.getId(),
                "member", testMember // <-- 핸들러가 기대하는 "member" 객체 추가
        );

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                mockAttributes, // "id"와 "member"가 모두 포함된 맵 전달
                "id"            // Principal의 .getName()이 "id" 키의 값을 반환하도록 설정
        );
        // --- 👆 여기까지 수정 👆 ---

        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // 리디렉션 URL을 캡처하기 위한 ArgumentCaptor
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        // response.sendRedirect()가 호출되었는지, 그리고 그 URL이 무엇인지 캡처
        String redirectUrl = response.getRedirectedUrl();

        // application-test.yml에 설정된 frontend.url 값("http://localhost:3000")을 확인
        assertThat(redirectUrl).startsWith("http://localhost:3000/oauth-redirect");
        assertThat(redirectUrl).contains("?token=mockToken");
        assertThat(redirectUrl).contains("&isNewUser=false"); // 1900-01-01이 아니므로 false
    }
}
