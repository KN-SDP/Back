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

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Value("${frontend.url}") // yml에 설정한 프론트엔드 URL 주입
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 인증된 Principal(주체) 객체를 가져옴
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 2. Principal의 'name' 속성(우리가 "id"로 설정한 값)을 가져옴
        Long userId = Long.parseLong(oAuth2User.getName());

        // 3. DB에서 전체 Member 정보를 조회 (토큰에 모든 정보를 담기 위해)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("OAuth2 로그인 오류: 사용자를 DB에서 찾을 수 없습니다."));

        // 4. JWT 토큰 생성
        String token = jwtUtil.generateToken(member);

        // 5. 토큰을 쿼리 파라미터로 포함하여 프론트엔드로 리디렉션
        // 예: https://knusdpsl.mooo.com/oauth-redirect?token=eyJh...
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect") // 프론트의 리디렉션 처리 페이지
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}