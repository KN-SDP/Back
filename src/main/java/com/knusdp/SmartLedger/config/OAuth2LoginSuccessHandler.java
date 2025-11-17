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
        try {
            log.info("✔️ [SuccessHandler] onAuthenticationSuccess 실행됨");

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            log.info("✔️ [SuccessHandler] authentication.getPrincipal(): {}", oAuth2User);

            // 1. UserService에서 지정한 Principal name 가져오기
            String userIdStr = oAuth2User.getName();
            log.info("➡️ [SuccessHandler] oAuth2User.getName() = {}", userIdStr);

            Long userId = Long.valueOf(userIdStr);
            log.info("➡️ [SuccessHandler] 파싱된 userId = {}", userId);

            // 2. DB에서 유저 찾기
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("OAuth2 오류: DB에 해당 ID의 사용자가 없습니다: " + userId));

            log.info("✔️ [SuccessHandler] DB Member 조회 성공: id={}, email={}", member.getId(), member.getEmail());

            boolean isNewUser = member.getBirth() == null || member.getBirth().isEqual(LocalDate.of(1900, 1, 1));
            log.info("➡️ [SuccessHandler] isNewUser = {}", isNewUser);

            // 3. JWT 생성
            String token;
            try {
                token = jwtUtil.generateToken(member);
                log.info("✔️ [SuccessHandler] JWT 생성 완료");
                log.info("🪪 [SuccessHandler] token = {}", token);
            } catch (Exception e) {
                log.error("❌ [SuccessHandler] JWT 생성 실패", e);
                String target = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                        .queryParam("error", "token_generation_failed")
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, target);
                return;
            }

            // 4. 최종 Redirect
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                    .queryParam("token", token)
                    .queryParam("isNewUser", isNewUser)
                    .build().toUriString();

            log.info("➡️ [SuccessHandler] 최종 Redirect URL = {}", targetUrl);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception ex) {
            log.error("❌ [SuccessHandler] onAuthenticationSuccess 처리 중 오류 발생", ex);

            String target = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                    .queryParam("error", "server_error")
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, target);
        }
    }
}