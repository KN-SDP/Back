package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 1. 소셜 로그인 플랫폼 정보 가져오기
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"

        // 2. 소셜 로그인 고유 ID 가져오기 (구글은 "sub")
        String providerId = (String) attributes.get("sub");

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 3. MemberService를 통해 소셜 유저를 찾거나 생성
        Member member = memberService.findOrCreateSocialUser(provider, providerId, email, name);

        // 4. Spring Security가 인증 시 사용할 Principal(주체) 객체 반환
        return new DefaultOAuth2User(
                Collections.emptyList(),
                // 'id' 속성에 우리 시스템의 PK(member.getId())를 저장
                Map.of("id", member.getId(), "email", email, "name", name),
                "id" // Principal의 .getName() 호출 시 "id" 키의 값을 반환
        );
    }
}