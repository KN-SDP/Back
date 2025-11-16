package com.knusdp.SmartLedger.config;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.service.MemberService;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        // 1) 원본 attributes 보존
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        // 2) 안전하게 값 꺼내기
        String providerId = (attributes.get("sub") != null) ? attributes.get("sub").toString() : null;
        String email = (attributes.get("email") != null) ? attributes.get("email").toString() : null;
        String name = (attributes.get("name") != null) ? attributes.get("name").toString() : null;

        Member member = memberService.findOrCreateSocialUser(provider, providerId, email, name);
        System.out.println("member = " + member);
        System.out.println("member id = " + member.getId());
        System.out.println("member email = " + member.getEmail());
        attributes.put("member", member);
        // ensure there's an id key as string
        attributes.put("id", member != null && member.getId() != null ? member.getId() : null);
        System.out.println("OAuth2User attributes: " + attributes);

        return new DefaultOAuth2User(
                // keep authorities from original user if present, otherwise grant a default
                oAuth2User.getAuthorities() == null || oAuth2User.getAuthorities().isEmpty()
                        ? Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
                        : oAuth2User.getAuthorities(),
                attributes,
                // nameAttributeKey: use a key that exists; prefer "sub" for Google
                attributes.containsKey("sub") ? "sub" : (attributes.containsKey("id") ? "id" : "email")

        );

    }
}
