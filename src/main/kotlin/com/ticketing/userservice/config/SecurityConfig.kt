package com.ticketing.userservice.config

import com.ticketing.userservice.security.jwt.JwtAuthenticationFilter
import com.ticketing.userservice.security.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 역할: Spring Security 관련 설정을 정의하는 클래스입니다.
 */
@Configuration
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Swagger 관련 경로에 대해 Spring Security의 모든 보안 검사를 무시하도록 설정합니다.
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

        val publicApiUrls = arrayOf(
            "/api/users/signup",
            "/api/users/login"
        )
        http
            // CSRF, HTTP Basic, Form Login 등 기본 보안 기능 비활성화
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers(*publicApiUrls).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider, redisTemplate),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}
