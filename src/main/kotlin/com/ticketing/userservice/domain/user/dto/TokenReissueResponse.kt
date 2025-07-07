package com.ticketing.userservice.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 재발급 응답 데이터 모델")
data class TokenReissueResponse(
    @Schema(description = "새로 발급된 JWT Access Token")
    val accessToken: String
)
