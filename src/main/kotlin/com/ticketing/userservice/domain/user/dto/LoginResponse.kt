package com.ticketing.userservice.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 로그인 성공 후, 발급된 Access Token을 클라이언트에게 전달하는 DTO
 */
@Schema(description = "로그인 성공 응답 데이터 모델")
class LoginResponse (
    @Schema(description = "JWT Access Token")
    val accessToken: String
)