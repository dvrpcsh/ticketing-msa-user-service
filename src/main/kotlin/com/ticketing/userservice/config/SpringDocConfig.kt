package com.ticketing.userservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

/**
 * SpringDoc(Swagger UI)에 대한 전역 설정을 정의하는 클래스
 * JWT인증을 위한 'Authorize' 버튼을 UI에 추가하고, 모든 API에 자물쇠 아이콘을 표시합니다.
 */
@Configuration
//1.OpenAPI 문서에 대한 전반적인 정보를 정의합니다.
@OpenAPIDefinition(
    info =  Info(
        title = "사용자 서비스  API",
        version =  "v1.0",
        description = "실시간 티켓 예매 플랫폼의 사용자 관련 기능을 제공하는 API 명세서입니다."
    ),
    //2.이 문서의 모든 API에 'bearerAuth'라는 이름의 보안 요구사항을 적용합니다.
    //이렇게 하면 모든 API 옆에 자물쇠 아이콘이 표시됩니다.
    security =  [SecurityRequirement(name = "bearerAuth")]
)
//3.'bearerAuth'라는 이름의 보안 스킴(Security Schema)을 정의합니다.
@SecurityScheme(
    name =  "bearerAuth",               //보안 스킴의 이름
    type =  SecuritySchemeType.HTTP,    //인증 타입은 HTTP
    bearerFormat = "JWT",               //토큰 형식은 JWT
    scheme = "bearer"                   //사용하는 인증 스킴은 Bearer 방식
)
class SpringDocConfig