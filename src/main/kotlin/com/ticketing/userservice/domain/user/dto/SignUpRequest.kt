package com.ticketing.userservice.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

//data class: 데이터를 담는 것을 주 목적으로 하는 클래스
data class SignUpRequest(

    //회원가입 시 외부(클라이언트)로부터 받을 이메일
    @Schema(description = "사용자 이메일 주소", example = "test@naver.com")
    val email: String,

    //비밀번호
    @Schema(description = "사용자 비밀번호", example = "password1234!")
    val password: String,

    //확인용 비밀번호
    @Schema(description = "비밀번호 동일여부 확인", example = "password1234!")
    val passwordConfirm: String,

    //사용자명
    @Schema(description = "사용자 이름", example = "최상협")
    val name: String
)
