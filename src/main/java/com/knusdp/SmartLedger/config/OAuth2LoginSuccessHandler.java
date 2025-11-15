package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Value("${frontend.url}") // yml에 설정한 프론트엔드 URL 주입
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. DB 조회를 제거하고, Principal의 속성에서 Member 객체를 직접 가져옴
        Member member = (Member) oAuth2User.getAttributes().get("member");

        if (member == null) {
            // "member" 속성이 없는 비상 상황 처리
            throw new RuntimeException("OAuth2 로그인 오류: 사용자 정보를 속성에서 찾을 수 없습니다.");
        }

        // 2. JWT 토큰 생성
        String token = jwtUtil.generateToken(member);

        // 3. 신규 유저인지 확인
        boolean isNewUser = member.getBirth().isEqual(LocalDate.of(1900, 1, 1));

        // 4. 프론트엔드로 리디렉션
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                .queryParam("token", token)
                .queryParam("isNewUser", isNewUser)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}