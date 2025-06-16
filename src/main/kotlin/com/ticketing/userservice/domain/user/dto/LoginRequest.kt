package com.ticketing.userservice.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 사용자 로그인을 요청할 때 사용하는 DTO
 */
@Schema(description = "사용자 로그인을 위한 요청 데이터 모델")
class LoginRequest (
    @Schema(description = "이메일", example = "test@naver.com")
    val email: String,

    @Schema(description = "비밀번호", example = "password1234!")
    val password: String
)