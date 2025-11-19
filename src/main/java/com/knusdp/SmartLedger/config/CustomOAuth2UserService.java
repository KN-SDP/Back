package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("➡️ [OAuth2] CustomOAuth2UserService.loadUser() 실행됨");
        log.info("➡️ [OAuth2] provider: {}", userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = null;
        String email = null;
        String name = null;

        // ------------ GOOGLE --------------
        if (provider.equals("google")) {
            log.info("✔ Google OAuth 처리");
            providerId = attributes.get("sub").toString();
            email = attributes.get("email").toString();
            name = attributes.get("name").toString();
        }

        if (provider.equals("kakao")) {
            log.info("➡️ [OAuth2] Kakao attributes = {}", attributes);

            providerId = attributes.get("id").toString();

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                if (kakaoAccount.get("email") != null) {
                    email = kakaoAccount.get("email").toString();
                }

                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null && profile.get("nickname") != null) {
                    name = profile.get("nickname").toString();
                }
            }
        }

        // DB 저장 / 조회
        Member member = memberService.findOrCreateSocialUser(provider, providerId, email, name);
        log.info("✔ Member 저장/조회 완료: memberId={}", member.getId());

        attributes.put("id", member.getId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id"  // getName() → memberId
        );
    }
}
