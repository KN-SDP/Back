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

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        String providerId = null;
        String email = null;
        String name = null;

        // ------------ GOOGLE --------------
        if (registrationId.equals("google")) {
            providerId = String.valueOf(attributes.get("sub"));
            email = String.valueOf(attributes.get("email"));
            name = String.valueOf(attributes.get("name"));
        }
        // ------------ KAKAO --------------
        else if (registrationId.equals("kakao")) {
            providerId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = String.valueOf(kakaoAccount.get("email"));
            name = (profile != null) ? String.valueOf(profile.get("nickname")) : "kakao_user";
        }
        // ------------ NAVER --------------
        else if (registrationId.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            providerId = String.valueOf(response.get("id"));
            email = String.valueOf(response.get("email"));
            name = String.valueOf(response.get("name"));
        }

        // 2. MemberService 호출 (신규 회원이면 null 반환, 기존 회원이면 Member 객체 반환)
        Member member = memberService.findOrCreateSocialUser(registrationId, providerId, email, name);

        // 3. 결과에 따라 속성 맵 구성 및 Principal Name 결정
        String principalNameKey;

        if (member != null) {
            // [기존 회원] -> 로그인 처리용 정보 담기
            log.info("✔ 기존 회원 로그인: memberId={}", member.getId());
            attributes.put("member", member);
            attributes.put("id", member.getId());
            principalNameKey = "id"; // 우리 DB의 ID를 주체로 사용
        } else {
            log.info("✔ 신규 회원 감지: 회원가입 페이지로 정보 전달");
            attributes.put("isNewUser", true);
            attributes.put("email", email);
            attributes.put("name", name);
            attributes.put("provider", registrationId);
            attributes.put("providerId", providerId);
            principalNameKey = userNameAttributeName;
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                principalNameKey
        );
    }
}