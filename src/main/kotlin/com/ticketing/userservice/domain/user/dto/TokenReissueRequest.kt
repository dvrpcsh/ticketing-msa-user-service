package com.ticketing.userservice.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 재발급 요청 데이터 모델")
data class TokenReissueRequest(
    @Schema(description = "만료되지 않은 Refresh Token")
    val refreshToken: String
)
