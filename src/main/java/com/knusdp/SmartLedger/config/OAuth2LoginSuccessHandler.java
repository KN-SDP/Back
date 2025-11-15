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
        Long userId = Long.parseLong(oAuth2User.getName());

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("OAuth2 로그인 오류: 사용자를 DB에서 찾을 수 없습니다."));

        String token = jwtUtil.generateToken(member);

        // --- 👇 '신규 유저'인지 확인하는 로직 추가 👇 ---
        boolean isNewUser = false;
        // MemberService에서 신규 유저 임시 생년월일을 '1900-01-01'로 설정했는지 확인
        if (member.getBirth().isEqual(LocalDate.of(1900, 1, 1))) {
            isNewUser = true;
        }

        // 5. 토큰 및 신규 유저 여부를 쿼리 파라미터로 포함하여 리디렉션
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                .queryParam("token", token)
                .queryParam("isNewUser", isNewUser) // "true" 또는 "false" 문자열로 전달됨
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}