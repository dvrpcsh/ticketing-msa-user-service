package com.ticketing.userservice.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 클라이언트의 모든 요청에 대해 JWT 토큰을 검사하고, 유효하다면 해당 요청의 인증 정보를 설정하는 필터
 * Spring Security의 필터 체인에서 UsernamePasswordAuthenticationFilter 앞에 위치하게 됩니다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() { //OncePerRequestFilter: 모든 서블릿 요청에 대해 한번만 실행되도록 보장

    /**
     * 실제 필터링 로직을 수행하는 메서드
     *
     * 1.클라이언트 요청의 HTTP 헤더에서 'Authorization' 헤더를 찾습니다.
     * 2.헤더에 'Bearer '로 시작하는 토큰이 있다면, 'Bearer '부분을 잘라내고 순수 토큰만 추출합니다.
     * 3.추출된 토큰이 유효한지 jwtTokenProvider를 통해 검증합니다.
     * 4.토큰이 유효하다면, 토큰에서 인증 정보(Authentication)를 가져와 SecurityContextHolder에 설정합니다.
     * -SecurityContextHolder에 인증정보가 설정되면, 해당 요청을 처리하는 동안 사용자는 '인증된' 상태가 됩니다.
     * 5.다음 필터로 요청을 전달합니다.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        //1~2 헤더에서 토큰 추출
        val token = resolveToken(request)

        //3~4 토큰 유효성 검증 및 인증 정보 설정
        if(token != null && jwtTokenProvider.validateToken(token)) {
            val authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        //5 다음 필터로 전달
        filterChain.doFilter(request, response)
    }

    //요청 헤더에서 'Bearer '토큰을 추출하는 헬퍼 메서드
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        return if(bearerToken != null &&  bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}