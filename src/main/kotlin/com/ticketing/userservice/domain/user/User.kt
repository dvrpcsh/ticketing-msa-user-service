package com.ticketing.userservice.domain.user

import com.ticketing.userservice.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name= "users") //데이터베이스 테이블 이름을 "users"로 지정
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, //PK, DB에서 자동 생성

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING) //Enum타입을 문자열로 저장
    @Column(nullable = false)
    var role: UserRole
) : BaseEntity() //2.BaseEntity를 상속받습니다.