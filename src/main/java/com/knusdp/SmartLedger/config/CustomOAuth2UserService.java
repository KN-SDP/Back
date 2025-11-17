package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.CallSite;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        log.info("➡️ [OAuth2] accessToken: {}", userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("✔ [OAuth2] 구글에서 유저 정보 가져옴: {}", oAuth2User.getAttributes());

        // 1. 원본 attributes 보존 및 수정을 위해 HashMap으로 복사
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = (attributes.get("sub") != null) ? attributes.get("sub").toString() : null;
        String email = (attributes.get("email") != null) ? attributes.get("email").toString() : null;
        String name = (attributes.get("name") != null) ? attributes.get("name").toString() : null;
        log.info("➡️ [OAuth2] providerId={}", providerId);
        log.info("➡️ [OAuth2] email={}", email);
        log.info("➡️ [OAuth2] name={}", name);

        log.info("➡️ [OAuth2] memberService.findOrCreateSocialUser() 호출됨");
        Member member = memberService.findOrCreateSocialUser(provider, providerId, email, name);
        log.info("✔ [OAuth2] 회원 조회/생성 완료. 우리 DB memberId={}", member.getId());

        // 2. attributes 맵에 우리 시스템의 정보 덮어쓰기
        attributes.put("id", member.getId()); // 우리 DB의 PK

        log.info("✔ [OAuth2] DefaultOAuth2User로 반환 (principal name=id)");

        // 3. Principal의 .getName()이 "id" 키의 값을 반환하도록 명시적으로 고정
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), // 기본 권한 부여
                attributes,
                "id" // <-- "sub"가 아닌 "id"로 고정!
        );
    }
}