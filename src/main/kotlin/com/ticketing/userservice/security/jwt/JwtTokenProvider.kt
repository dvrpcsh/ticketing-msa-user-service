package com.ticketing.userservice.security.jwt

import com.ticketing.userservice.domain.user.UserRole
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * Access Token과 Refresh Token을 생성하고, 유효성을 검증하며, 토큰에서 정보를 추출하는 핵심 클래스
 */
@Component
class JwtTokenProvider (
    // application.properties에 설정한 비밀키와 만료시간을 주입받음
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiration-time}") private val accessTokenExpirationTime: Long,
    @Value("\${jwt.refresh-token-expiration-time}") private val refreshTokenExpirationTime: Long
) {
    // 주입받은 비밀키를 HMAC-SHA 알고리즘에 사용할 수 있는 SecretKey 객체로 변환
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    }

    /**
     * 사용자의 이메일과 Role을 기반으로 Jwt Token(Access + Refresh)을 생성합니다.
     *
     */
    fun generateToken(email: String, role: UserRole): TokenPair {
        val accessToken = generateAccessToken(email, role)
        val refreshToken = generateRefreshToken(email)

        return TokenPair(accessToken, refreshToken)
    }

    //Access Token 생성
    private fun generateAccessToken(email: String, role: UserRole): String {
        val claims = Jwts.claims().apply { this["role"] = role.name }

        return doGenerateToken(claims, email, accessTokenExpirationTime)
    }

    //Refresh Token 생성
    private fun generateRefreshToken(email: String): String {

        return doGenerateToken(Jwts.claims(), email, refreshTokenExpirationTime)
    }

    //토큰생성로직
    private fun doGenerateToken(claims: Claims, subject: String, expireTime: Long): String {
        val now = Date()
        val validity = Date(now.time + expireTime)
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 주어진 JWT 토큰에서 인증(Authentication) 정보를 추출합니다.
     *
     */
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val role = claims["role"] as String? ?: throw RuntimeException("토큰에 역할정보가 없습니다.")
        val authorities = listOf(SimpleGrantedAuthority(role))

        return UsernamePasswordAuthenticationToken(claims.subject, "", authorities)
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     *
     */
    fun validateToken(token: String): Boolean {
        try {
            parseClaims(token)

            return true
        } catch(e: SecurityException) {
            //잘몬된 JWT 서명
        } catch(e: MalformedJwtException) {
            //유효하지 않은 JWT 토큰
        } catch(e: ExpiredJwtException) {
            //만료된 JWT 토큰
        } catch(e: UnsupportedJwtException) {
            //지원하지 않는 JWT토큰
        } catch(e: IllegalArgumentException) {
            //JWT Claims 문자열이 비어있는 경우
        }
        return false
    }

    /**
     * 토큰의 남은 유효 시간을 계산하여 밀리초 단위로 반환합니다.
     */
    fun getRemainingTime(token: String): Long {
        val expiration = parseClaims(token).expiration

        return expiration.time - Date().time
    }

    /**
     * 토큰 파싱을 위한 공통 메서드
     */
    private fun parseClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}

//생성된 토큰을 담을 데이터 클래스
data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)