package com.ticketing.userservice.config

import com.ticketing.userservice.security.jwt.JwtAuthenticationFilter
import com.ticketing.userservice.security.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 역할: Spring Security 관련 설정을 정의하는 클래스입니다.
 * 공개용 경로와 인증용 경로의 보안 필터 체인을 분리하여 충돌방지
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
     * [보안 체인 1 - 공개용]인증이 전혀 필요없는 경로들을 처리합니다.
     * @Order(1) 어노테이션으로 가장 먼저 검사하도록 설정합니다.
     */
    @Bean
    @Order(1)
    fun publicEndpointsFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            //이 체인이 처리할 경로들을 지정합니다.
            securityMatcher(
                "/api/users/signup",
                "/api/users/login",
                "/api/users/reissue",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            )
            //위 경로들에 대해서는 모든 보안 기능을 비활성화하고 모든 접근을 허용합니다.
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            csrf { disable() }
            headers { frameOptions { disable() } } //Swagger UI의 iframe을 위해 필요할 수 있습니다.
        }

        return http.build()
    }

    /**
     * [보안 체인 2 - 인증용]JWT인증이 필요한 모든 API경로를 처리합니다.
     */
    @Bean
    @Order(2)
    fun privateEndpointsFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            //이 체인은 /api/로 시작하는 모든 경로를 대상으로 합니다.
            securityMatcher("/api/**")

            csrf{ disable() }
            httpBasic { disable() }
            formLogin { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            //모든 요청은 반드시 인증을 거쳐야 함
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }

            //우리가 직접 만든 JWT 인증 필터를 이 체인에만 추가합니다.
            addFilterBefore<UsernamePasswordAuthenticationFilter>(JwtAuthenticationFilter(jwtTokenProvider,redisTemplate))
        }

        return http.build()
    }
}
