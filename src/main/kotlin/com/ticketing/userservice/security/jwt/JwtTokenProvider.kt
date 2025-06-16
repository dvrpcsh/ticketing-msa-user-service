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
 * JWT를 생성하고, 유효성을 검증하며, 토큰에서 정보를 추출하는 핵심 클래스
 */
@Component
class JwtTokenProvider (
    // application.properties에 설정한 비밀키와 만료시간을 주입받음
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-time}") private val expirationTime: Long

) {
    // 주입받은 비밀키를 HMAC-SHA 알고리즘에 사용할 수 있는 SecretKey 객체로 변환
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    }

    /**
     * 사용자의 이메일과 Role을 기반으로 Jwt Token을 생성합니다.
     *
     * 1.Claims(토큰에 담을 정보)를 설정합니다. 여기서는 사용자 역할(Role)을 담슴니다.
     * 2.현재 시간과 만료 시간을 설정합니다.
     * 3.최종적으로 토큰을 빌드하고, 비밀 키로 서명하여 문자열 형태로 반환합니다.
     */
    fun generateToken(email: String, role: UserRole): String {
        val claims = Jwts.claims().apply {
            this["role"] = role.name
        }
        val now = Date()
        val validity = Date(now.time + expirationTime)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email) //토큰의 주체(subject)로 사용자의 이메일을 사용합니다.
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 주어진 JWT 토큰에서 인증(Authentication) 정보를 추출합니다.
     *
     * 1.토큰을 파싱하여 Claims를 추출합니다.
     * 2.Claims에서 사용자 이메일(subject)과 역할(Role) 정보를 가져옵니다.
     * 3.Spring Security가 이해할 수 있는 Authentication 객체(UsernamePasswordAuthenticationToken)을 생성하여 반환합니다.
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        val email = claims.subject
        val role = claims["role"] as String
        val authorities = listOf(SimpleGrantedAuthority(role))

        return UsernamePasswordAuthenticationToken(email, "", authorities)
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     *
     * 1.토큰을 파싱해보고, 각종 예외(서명 불일치, 만료, 형식 오류 등)가 발생하면 false를 반환
     * 2.예외 없이 성공적으로 파싱되면 토큰이 유효한 것이므로 true를 반환
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)

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
}