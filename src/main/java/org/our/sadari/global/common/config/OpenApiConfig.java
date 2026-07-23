package org.our.sadari.global.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI와 OpenAPI 문서의 기본 정보를 설정합니다.
 * 프로젝트 인증은 쿠키의 accessToken을 사용하므로 Swagger 문서에도 같은 인증 스키마를 노출합니다.
 *
 * @author Seunghyeon.Kang
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sadari API",
                description = "독서 기록, 독후감, 목표, 소셜 프로필 기능을 제공하는 Sadari 백엔드 API 문서",
                version = "v1"
        )
)
@SecurityScheme(
        name = "accessTokenCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "accessToken",
        description = "로그인 후 발급되는 accessToken 쿠키"
)
public class OpenApiConfig {
}
