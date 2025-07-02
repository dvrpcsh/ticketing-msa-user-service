package com.ticketing.userservice.config

import com.ticketing.userservice.security.jwt.JwtAuthenticationFilter
import com.ticketing.userservice.security.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

/**
 * 역할: Spring Security 관련 설정을 정의하는 클래스입니다.
 */
@Configuration
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Swagger 관련 경로에 대해 Spring Security의 모든 보안 검사를 무시하도록 설정합니다.
     * 이 설정은 SecurityFilterChain보다 먼저 동작하여, 리다이렉트를 포함한 모든 Swagger 관련 요청을 허용합니다.
     */
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                )
        }
    }

    /**
     * 역할: HTTP 요청에 대한 보안 규칙(인증/인가)을 설정합니다.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // CSRF, HTTP Basic, Form Login 등 기본 보안 기능 비활성화
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }

            // JWT 인증을 위해 세션은 사용하지 않음
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            // [최종 수정] 경로별 인가(Authorization) 규칙을 명확하게 재설정합니다.
            authorizeHttpRequests {
                authorize("/api/users/signup", permitAll)
                authorize("/api/users/login", permitAll)
                // Swagger UI 접근을 위한 모든 경로를 명시적으로 허용합니다.
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                // 그 외 모든 API는 인증을 요구합니다.
                authorize(anyRequest, authenticated)
            }

            // 우리가 직접 만든 JWT 인증 필터를 Security 필터 체인에 추가
            addFilterBefore<UsernamePasswordAuthenticationFilter>(JwtAuthenticationFilter(jwtTokenProvider))
        }
        return http.build()
    }
}
