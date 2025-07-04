package com.ticketing.userservice.domain.user

import com.ticketing.userservice.domain.user.dto.LoginRequest
import com.ticketing.userservice.domain.user.dto.LoginResponse
import com.ticketing.userservice.domain.user.dto.SignUpRequest
import com.ticketing.userservice.domain.user.dto.MyInfoResponse
import com.ticketing.userservice.security.jwt.JwtTokenProvider
import com.ticketing.userservice.security.jwt.TokenPair
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import java.util.concurrent.TimeUnit
import java.time.Duration

@Service
class UserService (

    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    //모든 트랜잭션을 하나로 묶어 중간에 실패하면 모두 Rollback되게 함
    @Transactional
    fun signUp(request: SignUpRequest) {
        //이메일 중복 확인
        //사용자가 요청한 이메일로 이미 가입한 회원이 있는지 DB에서 찾아봄
        if(userRepository.findByEmail(request.email) != null) {
            //이미 존재한다면, 예외처리 후 가입 중단
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
     * 1.이메일로 사용자를 조회합니다.(없으면 예외발생)
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

        val tokenPair = jwtTokenProvider.generateToken(user.email, user.role)

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
     * 1.SecurityContextHolder에서 현재 사용자의 인증 정보(Authentication)를 가져옵니다.
     * - 이 정보는 이전에 만든 JwtAuthenticationFilter가 토큰을 검증하고 넣어준 것 입니다.
     * 2.인증 정보에서 사용자의 이메일(name)을 추출합니다.
     * 3.이메일을 사용하여 DB에서 사용자 정보를 조회하고, MyInfoResponse DTO로 변환하여 반환합니다.
     */
    fun getMyInfo(): MyInfoResponse {
        val email = SecurityContextHolder.getContext().authentication.name

        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다.")

        return MyInfoResponse.from(user)
    }

    /**
     * 사용자 로그아웃
     *
     * 1.전달받은 Access Token을 Redis의 Denylist에 등록하여 무효화합니다.(남은 유효기간만큼만 저장)
     * 2.Redis에 저장되어 있던 사용자의 Refresh Token을 삭제하여 재발급을 막습니다.
     */
    fun logout(accessToken: String) {
        if(!jwtTokenProvider.validateToken(accessToken)) {
            throw IllegalArgumentException("유효하지 않은 토큰입니다.")
        }

        val authentication = jwtTokenProvider.getAuthentication(accessToken)
        val email = authentication.name

        //1.Redis에서 해당 사용자의 Refresh Token 삭제
        if(redisTemplate.opsForValue().get("RT: $email") != null) {
            redisTemplate.delete("RT: $email")
        }

        //2.Access Token을 Denylist에 등록
        val remainingTime = jwtTokenProvider.getRemainingTime(accessToken)
        if(remainingTime > 0) {
            redisTemplate.opsForValue().set(accessToken, "logout", Duration.ofMillis(remainingTime))
        }
    }
}