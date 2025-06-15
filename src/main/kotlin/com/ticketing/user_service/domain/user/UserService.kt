package com.ticketing.user_service.domain.user

import com.ticketing.user_service.domain.user.User
import com.ticketing.user_service.domain.user.UserRepository
import com.ticketing.user_service.domain.user.UserRole
import com.ticketing.user_service.domain.user.dto.SignUpRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService (

    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
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
        var user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = UserRole.USER
        )

        //User저장
        userRepository.save(user)
    }
}