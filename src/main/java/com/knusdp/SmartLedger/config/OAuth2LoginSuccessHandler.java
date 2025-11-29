package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.auth.AccountDeletedException;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Value("${frontend.url}") // yml에 설정한 프론트엔드 URL 주입
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 1. 기존 회원인 경우 (Member 객체가 있음) -> 로그인 처리
        if (attributes.containsKey("member")) {
            Member member = (Member) attributes.get("member");

           // 탈퇴한 계정이면 예외 던짐
            if (Boolean.TRUE.equals(member.getDeleted())) {
                log.warn("❌ 탈퇴된 소셜 계정 로그인 시도: {}", member.getEmail());

                throw new AccountDeletedException(
                        String.format("탈퇴된 계정입니다. 14일 이내 복구가 가능합니다. [%s]", member.getEmail())
                );

            }
            String token = jwtUtil.generateToken(member); // 로그인용 Access Token

            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                    .queryParam("token", token)
                    .queryParam("isNewUser", false)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // 2. 신규 회원인 경우 -> 회원가입 페이지로 정보 전달
        if (Boolean.TRUE.equals(attributes.get("isNewUser"))) {
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String providerId = (String) attributes.get("providerId");
            String provider = (String) attributes.get("provider");

            // 회원가입용 임시 토큰 생성 (이메일, 이름 등 정보 포함)
            // 주의: 이 토큰은 로그인용이 아니라, 회원가입 폼을 채우기 위한 용도입니다.
            String registerToken = jwtUtil.generateRegisterToken(email, name, provider, providerId);

            // 프론트엔드 회원가입 페이지로 리다이렉트 (토큰 전달)
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/signup") // 회원가입 페이지 경로
                    .queryParam("registerToken", registerToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}