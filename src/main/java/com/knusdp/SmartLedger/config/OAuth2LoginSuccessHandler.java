package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Object rawMember = oAuth2User.getAttributes().get("member");
            System.out.println("[OAuth2LoginSuccessHandler] rawMember = " + rawMember);
            if (!(rawMember instanceof Member)) {
                System.out.println("[OAuth2LoginSuccessHandler] rawMember is not instance of Member");
                // fallback: DB에서 이메일 또는 id로 조회
                String email = (String) oAuth2User.getAttributes().get("email");
                Long id = null;
                Object idObj = oAuth2User.getAttributes().get("id");
                if (idObj != null) {
                    try { id = Long.valueOf(idObj.toString()); } catch (Exception ignored) {}
                }
                Member member = null;
                if (id != null) member = memberRepository.findById(id).orElse(null);
                if (member == null && email != null) member = memberRepository.findByEmail(email).orElse(null);

                if (member == null) {
                    // 안전한 대체 행동: 신규회원 리다이렉트 or 에러 페이지
                    String target = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                            .queryParam("error", "no_member")
                            .build().toUriString();
                    getRedirectStrategy().sendRedirect(request, response, target);
                    return;
                } else {
                    rawMember = member;
                }
            }

            Member member = (Member) rawMember;

            // 안전 방어: birth가 null이면 신규 유저로 간주
            boolean isNewUser = member.getBirth() == null || member.getBirth().isEqual(LocalDate.of(1900, 1, 1));

            // JWT 생성 — 예외 잡기
            String token;
            try {
                token = jwtUtil.generateToken(member);
            } catch (Exception e) {
                // token 생성 실패 시 로그 찍고 에러 리다이렉트
                logger.error("JWT 생성 실패", e);
                String target = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                        .queryParam("error", "token_generation_failed")
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, target);
                return;
            }

            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                    .queryParam("token", token)
                    .queryParam("isNewUser", isNewUser)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception ex) {
            logger.error("OAuth2 onAuthenticationSuccess 처리 중 오류", ex);
            // 에러 시 프론트로 에러코드 전달
            String target = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                    .queryParam("error", "server_error")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, target);
        }
    }


}