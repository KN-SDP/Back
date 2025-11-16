package com.knusdp.SmartLedger.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService; // <-- 1. 핸들러 주입
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 1. 인증 없이 접근을 허용할 URL들을 명시적으로 지정합니다.
                        .requestMatchers(
                                "/users/login",
                                "/users/sign-up",
                                "/swagger-ui/*",
                                "/swagger-ui.html",
                                "/users/recover-id",
                                "/users/recover-password",
                                "/users/recover-password/reset",
                                "/login/oauth2/code/**", // <-- 3. OAuth2 리디렉션 경로 허용
                                "/oauth2/**", // <-- 4. OAuth2 로그인 URL 허용
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/login", // <-- /login 경로 추가
                                "/login?error",
                                "/oauth-redirect"

                        ).permitAll()
                        // 2. 위에서 허용한 URL을 제외한 나머지 모든 요청은 인증이 필요합니다.
                        //    (예: /users/changeNickname, /ledger 등)
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정보 처리
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 후 JWT 발급/리디렉션 처리
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"status\": 401, \"error\": \"UNAUTHORIZED\", \"message\": \"인증이 필요합니다.\"}"
                            );
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 접속을 허용할 프론트엔드 주소를 명시적으로 등록합니다.
        configuration.setAllowedOriginPatterns(List.of(
                "https://knusdpsl.mooo.com", // 실제 배포된 프론트엔드 도메인
                "http://localhost:3000",     // 로컬 React 개발용
                "http://localhost:8081",      // 로컬 React Native Metro 서버
                "https://web-front-mhvqlrzi1b9d488f.sel3.cloudtype.app",
                "https://d76cce2e.front-4ob.pages.dev"
        ));

        // 2. 허용할 HTTP 메소드를 지정합니다.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 3. 허용할 HTTP 헤더를 지정합니다. ("*"로 모든 헤더 허용)
        configuration.setAllowedHeaders(List.of(
                "*"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));

        // 4. 특정 도메인을 명시했으므로 'true'로 설정하여 JWT 토큰을 주고받을 수 있게 합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용

        return source;
    }
}