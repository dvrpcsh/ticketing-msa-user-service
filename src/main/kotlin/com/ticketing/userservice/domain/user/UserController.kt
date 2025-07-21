package com.ticketing.userservice.domain.user

import com.ticketing.userservice.domain.user.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "사용자 API", description = "사용자 가입, 조회 등 관련 기능을 제공하는 API입니다.")
@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    /**
     * 사용자 회원가입 API
     *
     * 1.클라이언트로부터 회원가입에 필요한 정보를 HTTP Body로 받습니다.
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

    /**
     * 사용자 로그인 API
     *
     * 1.클라이언트로부터 로그인 정보(LoginRequest)를 HTTP Body로 받습니다.
     * 2.UserService의 login 메서드를 호출하여 Access/Refresh토큰이 담긴 LoginResponse를 받습니다.
     * 3.받은 LoginResponse를 그대로 클라이언트에게 응답합니다.
     */
    @Operation(summary = "사용자 로그인")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        //LoginResponse객체를 반환합니다.
        val loginResponse = userService.login(request)

        return ResponseEntity.ok(loginResponse)
    }

    /**
     * 내 정보 조회 API
     *
     * API 게이트웨이가 검증 후 헤더에 담아준 사용자 이메일을 받습니다.
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인 된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    fun getMyInfo(@RequestHeader("X-User-Email") email: String): ResponseEntity<MyInfoResponse> {
        val myInfo = userService.getMyInfo(email)
        return ResponseEntity.ok(myInfo)
    }


    /**
     * 사용자 로그아웃 API
     *
     * 게이트웨이가 검증 후 헤더에 담아준 Access Token과 사용자 이메일을 받습니다.
     */
    @Operation(summary = "사용자 로그아웃", description = "현재 로그인 된 사용자를 로그아웃 처리합니다.(인증 필요)")
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestHeader("X-User-Email") email: String
    ): ResponseEntity<String> {

        //"Bearer "를 제거하기 위해 7번째 문자부터 자릅니다.
        val accessToken = authorizationHeader.substring(7)
        userService.logout(accessToken, email)

        return ResponseEntity.ok("성공적으로 로그아웃되었습니다.")
    }

    /**
     * Access Token 재발급 API
     *
     * 1.클라이언트로부터 Refresh Token을 HTTP Body로 받습니다.
     * 2.UserService의 reissueToken 메서드를 호출하여 새로운 Access Token을 발급받습니다.
     * 3.발급받은 토큰을 클라이언트에게 응답합니다.
     */
    @Operation(summary = "Access Token 재발급")
    @PostMapping("/reissue")
    fun reissueToken(@RequestBody request: TokenReissueRequest): ResponseEntity<TokenReissueResponse> {
        val response = userService.reissueToken(request)

        return ResponseEntity.ok(response)
    }
}