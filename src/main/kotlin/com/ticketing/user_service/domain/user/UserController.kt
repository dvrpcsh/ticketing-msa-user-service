package com.ticketing.user_service.domain.user

import com.ticketing.user_service.domain.user.dto.SignUpRequest
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "사용자 API", description = "사용자 가입, 조회 등 관련 기능을 제공하는 API입니다.")
@RestController
@RequestMapping("/api/users")
class UserController (

    private val userService: UserService
) {
    /**
     * 사용자 회원가입 API
     *
     * 1.클라이언트로부터 회원가입에 필요한 정보(SignUpRequest)를 HTTP Body로 받습니다.
     * 2.비즈니스 로직 처리를 위해 UserService의 signUp메서드를 호출합니다.
     * 3.처리가 성공적으로 완료되면, HTTP 201 Created 상태 코드와 성공 메시지를 반환합니다.
     */
    @Operation(summary = "사용자 회원가입", description = "이메일, 비밀번호, 이름 등을 입력받아 신규 사용자를 생성합니다.")
    @PostMapping("/signup")
    fun signUp(
        //HTTP요청의 본문(body)에 담겨오는 JSON데이터를 SignUpRequest DTO객체로 변환해줍니다.
        @RequestBody request: SignUpRequest
    ): ResponseEntity<String> {

        //주입받은 UserService의 SignUp 메서드를 호출하여 비즈니스 로직을 수행합니다.
        userService.signUp(request)

        //모든 로직이 성공적으로 끝나면, HTTP 상태코드 201(Created)과 함께 성공 메시지를 담아 응답합니다.
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("회원가입이 완료되었습니다.")
    }
}