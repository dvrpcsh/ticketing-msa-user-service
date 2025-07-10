package com.ticketing.userservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * user-service의 보안설정
 * 모든 인증은 API 게이트웨이에서 처리하는 것을 전제로 하므로,
 * 이 서비스는 모든 요청을 허용하도록 설정을 단순화합니다.
 */
@Configuration
class SecurityConfig{

    @Bean
    fun passwordEncoder() : PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            //게이트웨이를 통해 들어오는 요청은 신뢰하므로, CSRF 등의 보호 기능을 비활성화합니다.
            csrf { disable() }
            //모든 요청을 허용합니다.(인증/인가는 게이트웨이에 책임)
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
        }

        return http.build()
    }
}
