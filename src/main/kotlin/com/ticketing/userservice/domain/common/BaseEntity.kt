package com.ticketing.userservice.domain.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
//다른 클래스가 상속할 수 있게 추상 클래스로 선언함
abstract class BaseEntity {

    //Entity가 처음으로 데이터베이스에 저잘될 때 현재 시간을 자동으로 기록합니다.
    @CreatedDate

    //updatable = false 이 필드는 생성된 이후에는 절대 수정되지 않도록 설정합니다.
    //nullable = flase 데이터베이스에 저장될 때 null 허용x
    @Column(updatable = false, nullable = false)
    //lateinit: '나중에 초기화될 것'을 의미하며, non-null 타입이지만 생성자에서 초기화하지 않아도 되게 함
    lateinit var createdAt: LocalDateTime

    //Entity가 수정될 때 마다 현재 시간을 자동으로 기록합니다.
    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime
}