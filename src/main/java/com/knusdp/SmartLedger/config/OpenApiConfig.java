package com.knusdp.SmartLedger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() { // 메소드 이름은 openAPI() 그대로 사용해도 됩니다.
        final String securitySchemeName = "bearerAuth"; // 보안 스키마의 이름

        return new OpenAPI()
                // 기존 API 정보 설정
                .info(new Info()
                        .title("SmartLedger API")
                        .version("1.0.0") // 기존 버전 유지 또는 v1으로 변경
                        .description("SmartLedger API 문서"))
                // 모든 API 엔드포인트에 'bearerAuth' 보안 요구사항 추가
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 'bearerAuth'라는 이름의 보안 스키마 정의
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)        // 스키마 이름
                                .type(SecurityScheme.Type.HTTP) // 타입: HTTP
                                .scheme("bearer")               // 스키마: Bearer
                                .bearerFormat("JWT")));          // 형식: JWT
    }
}
