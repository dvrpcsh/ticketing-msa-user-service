package com.ticketing.userservice.config

import com.ticketing.userservice.security.jwt.JwtTokenProvider
import com.ticketing.userservice.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * 역할: Spring Security 관련 설정을 정의하는 클래스입니다.
 */
@Configuration
class SecurityConfig (
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * 역할: HTTP 요청에 대한 보안 규칙(인증/인가)을 설정합니다.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        // 공개적으로 접근을 허용할 URL 목록을 정의합니다.
        val publicUrls = arrayOf(
            "/api/users/signup",
            "/api/users/login",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        )

        /**
         * 흐름:
         * 1. CORS 설정을 적용합니다.
         * 2. 사용하지 않는 기본 보안 기능(CSRF, HTTP Basic, Form Login)을 비활성화합니다.
         * 3. 세션 관리 정책을 STATELESS로 설정하여, 서버가 세션을 새엇ㅇ하거나 사용하지 않도록 합니다.
         * 4. publicUrls에 등록된 경로는 인증 없이 모두 접근을 허용하고, 그 외의 모든 요청은 인증을 요구하도록 설정합니다.
         * 5. 우리가 직접 만든 JwtAuthenticationFilter를 Spring Security의 필터 체인에 추가합니다.
         * (UsernamePasswordAuthenticationFilter앞에 추가하여, 모든 요청이 Controller에 도달하기 전에 토큰 검사르 먼저 거치도록 함
         */
        http
            .cors { it.configurationSource(corsConfigurationSource()) } // 1. CORS 설정 적용
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //3.세션 STATELESS설정
            }
            .authorizeHttpRequests {
                it.requestMatchers(*publicUrls).permitAll() // 4. 공개 URL 허용
                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

/**
 * 역할: CORS(Cross-Origin Resource Sharing) 관련 설정을 정의합니다.
 * 흐름:
 * 1. 우리 서버로 들어오는 모든 경로()에 대해 CORS 정책을 적용합니다.
 * 2. http://localhost:8000 (API 게이트웨이)からの 요청을 허용합니다.
 * 3. 허용할 HTTP 메서드(GET, POST 등)를 지정합니다.
 * 4. 모든 요청 헤더를 허용합니다.
*/
@Bean
fun corsConfigurationSource(): CorsConfigurationSource {
val configuration = CorsConfiguration()
configuration.allowedOrigins = listOf("http://localhost:8000") // 게이트웨이 주소
configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
configuration.addAllowedHeader("*")
configuration.allowCredentials = true

val source = UrlBasedCorsConfigurationSource()
source.registerCorsConfiguration("/**", configuration) // 모든 경로에 대해 위 설정을 적용
return source
}
}
