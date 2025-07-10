package com.ticketing.userservice.domain.user

import com.ticketing.userservice.domain.user.dto.*
import com.ticketing.userservice.security.jwt.JwtTokenProvider
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Duration

@Service
class UserService (

    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    /**
     * 신규 사용자 회원가입
     *
     * 1.요청된 이메일이 이미 존재하는지 확인합니다.
     * 2.비밀번호와 비밀번호 확인 값이 일치하는지 검사합니다.
     * 3.비밀번호를 암호화하여 새로운 User 엔티티를 생성하고 데이터베이스에 저장합니다.
     */
    @Transactional
    fun signUp(request: SignUpRequest) {

        if(userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        //비밀번호 확인
        if(request.password != request.passwordConfirm) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        //User Entity생성
        //DTO의 데이터를 기반으로 새로운 User객체를 만듭니다.
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = UserRole.USER
        )

        //User저장
        userRepository.save(user)
    }

    /**
     * 사용자 로그인
     *
     * 1.이메일로 사용자를 조회하고 비밀번호가 일치하는지 확인합니다.
     * 2.인증 성공 시, Access Token과 Refresh Token을 모두 생성합니다.
     * 3.생성된 Refresh Token은 Redis에 저장하여 관리합니다. (Key: "RT:{email}", Value: refreshToken)
     * 4.두 토큰을 모두 클라이언트에게 반환합니다.
     */
    fun login(request: LoginRequest): LoginResponse {
        //1.사용자 확인
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")

        //2.비밀번호 확인
        if(!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")
        }

        val tokenPair = jwtTokenProvider.generateTokenPair(user.email, user.role)

        //Refresh Token을 Redis에 저장(유효기간과 동일하게 설정)
        redisTemplate.opsForValue().set(
            "RT:${user.email}",
            tokenPair.refreshToken,
            Duration.ofMillis(jwtTokenProvider.getRemainingTime(tokenPair.refreshToken))
        )

        return LoginResponse(tokenPair.accessToken, tokenPair.refreshToken)
    }

    /**
     * 현재 인증된 사용자의 정보를 조회합니다.
     *
     * 컨트롤러로부터 전달받은 이메일을 사용하여 DB에서 사용자 정보를 조회합니다.
     */
    fun getMyInfo(email: String): MyInfoResponse {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다.")

        return MyInfoResponse.from(user)
    }

    /**
     * 사용자 로그아웃
     *
     * 컨트롤러로부터 Access Token과 이메일을 모두 전달받아 처리합니다.
     */
    fun logout(accessToken: String, email: String) {

        /* 유효성 검증은 게이트웨이에서 진행
        if(!jwtTokenProvider.validateToken(accessToken)) {
            throw IllegalArgumentException("유효하지 않은 토큰입니다.")
        }
        */

        val authentication = jwtTokenProvider.getAuthentication(accessToken)
        val email = authentication.name

        //1.Redis에서 해당 사용자의 Refresh Token 삭제
        val refreshTokenKey = "RT:$email"
        if (redisTemplate.hasKey(refreshTokenKey)) {
            redisTemplate.delete(refreshTokenKey)
        }

        //2.Access Token을 Denylist에 등록
        val remainingTime = jwtTokenProvider.getRemainingTime(accessToken)
        if(remainingTime > 0) {
            redisTemplate.opsForValue().set(accessToken, "logout", Duration.ofMillis(remainingTime))
        }
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token을 재발급합니다.
     *
     * 1.전달받은 Refresh Token의 유효성을 검증합니다.
     * 2.토큰에서 사용자의 이메일 정보를 추출합니다.
     * 3.Redis에 저장된 Refresh Token과 전달받은 토큰이 일치하는지 확인하여 탈취여부를 검증합니다.
     * 4.모든 검증을 통과하면 새로운 Access Token을 생성하여 반환합니다.
     */
    fun reissueToken(request: TokenReissueRequest): TokenReissueResponse {
        val refreshToken = request.refreshToken

        //1.Refresh Token 유효성 검증
        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 Refresh Token입니다.")
        }

        //2.토큰에서 사용자 정보 추출
        val email = jwtTokenProvider.getSubject(refreshToken)

        //3.Redis에 저장된 토큰과 일치하는지 확인
        val savedRefreshToken = redisTemplate.opsForValue().get("RT:$email")
        if(savedRefreshToken != refreshToken) {
            throw IllegalArgumentException("Refresh Token 정보가 일치하지 않습니다.")
        }

        //4.새로운 Access Token 생성
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다.")
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.email, user.role)

        return TokenReissueResponse(newAccessToken)
    }
}