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
        // attributes를 먼저 꺼냄
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // 1) 우리가 CustomOAuth2UserService에서 넣어준 "id"가 있으면 그걸 사용
        String idAttr = attrs.get("id") != null ? String.valueOf(attrs.get("id")) : null;
        Long userId = null;

        if (idAttr != null) {
            try {
                userId = Long.valueOf(idAttr);
            } catch (NumberFormatException e) {
                // 이상치면 무시하고 다음 방식으로 시도
                userId = null;
            }
        }

        // 2) 아직 userId가 없으면 provider의 고유 ID(sub 등)로 DB 조회
        if (userId == null) {
            // 구글은 "sub", 다른 provider는 "id" 또는 registration에 따라 다를 수 있음
            String providerId = attrs.get("sub") != null ? String.valueOf(attrs.get("sub")) :
                    attrs.get("id") != null ? String.valueOf(attrs.get("id")) : null;

            if (providerId != null) {
                // MemberRepository에서 providerId로 찾음 (Member.providerId는 String 타입)
                memberRepository.findByProviderId(providerId).ifPresent(m -> {
                    // 값을 직접 세팅할 수 없으므로 외부에서 처리
                });
                var memberOpt = memberRepository.findByProviderId(providerId);
                if (memberOpt.isPresent()) {
                    userId = memberOpt.get().getId();
                }
            }
        }

        // 3) 그래도 못 찾으면 이메일로 시도 (email이 있을 때만)
        if (userId == null) {
            String email = attrs.get("email") != null ? String.valueOf(attrs.get("email")) : null;
            if (email != null) {
                userId = memberRepository.findByEmail(email)
                        .map(Member::getId)
                        .orElseThrow(() -> new RuntimeException("OAuth2 로그인 오류: 사용자를 DB에서 찾을 수 없습니다."));
            } else {
                throw new RuntimeException("OAuth2 로그인 오류: 사용자 정보를 찾을 수 없습니다.");
            }
        }

        // userId 확보됨 -> member 로드
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("OAuth2 로그인 오류: 사용자를 DB에서 찾을 수 없습니다."));

        String token = jwtUtil.generateToken(member);

        boolean isNewUser = member.getBirth().isEqual(LocalDate.of(1900, 1, 1));

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                .queryParam("token", token)
                .queryParam("isNewUser", isNewUser)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}