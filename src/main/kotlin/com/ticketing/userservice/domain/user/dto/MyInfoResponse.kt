package com.ticketing.userservice.domain.user.dto

import com.ticketing.userservice.domain.user.User
import io.swagger.v3.oas.annotations.media.Schema

/**
 * '내 정보 조회' API의 응답 데이터 모델
 */
data class MyInfoResponse (
    @Schema(description = "사용자 ID")
    val id: Long,

    @Schema(description = "이메일")
    val email: String,

    @Schema(description = "이름")
    val name: String
) {
    companion object {
        // User Entity를 MyInfoResponse DTO로 변환하는 정적 팩토리 메서드
        fun from(user: User): MyInfoResponse {
            return MyInfoResponse(
                id =  user.id!!,
                email = user.email,
                name = user.name
            )
        }
    }
}