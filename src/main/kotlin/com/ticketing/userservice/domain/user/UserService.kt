package com.ticketing.userservice.domain.user

import com.ticketing.userservice.domain.user.dto.LoginRequest
import com.ticketing.userservice.domain.user.dto.SignUpRequest
import com.ticketing.userservice.security.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService (

    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
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
     * 2.입력된 비밀번호와 DB에 저장된 암호화된 비밀번호가 일치하는지 확인합니다.
     * 3.비밀번호가 일치하면, 해당 사용자의 이메일과 Role을 기반으로 JWT 토큰을 생성하여 반환합니다.
     */
    fun login(request: LoginRequest): String {
        //1.사용자 확인
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")

        //2.비밀번호 확인
        if(!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")
        }

        //3.JWT 토큰 생성
        return jwtTokenProvider.generateToken(user.email, user.role)
    }
}