package com.ticketing.userservice.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

//[1] @Repository 어노테이션:
//해당 인터페이스가 데이터베이스와 통신하는 레포지토리임을 Spring framework에 명시
//Spring이 이 인터페이스를 찾아서 자동으로 Bean으로 등록하고 관리함
@Repository
interface UserRepository : JpaRepository<User, Long>{
    //[2] JpaRepository<User, Long> 상속:
    //두 개의 제네릭 타입을 받습니다.
    //첫 번째(User): 이 레포지토리가 어떤 Entity를 다룰 것인지 명시
    //두 번째(Long): 해당 Entity의 PK의 타입이 무엇인지 명시

    //[3] 기본CRUD 메서드 자동 구현
    // JpaRepository를 상속받는 것만으로 아래와 같은 기본적인 메서드 사용가능
    // - save(user): User객체를 저장하고 반환
    // - findById(id): Long타입의 id로 User를 찾아 Optional<User>로 반환
    // - findAll(): 모든 User목록을 List<User>로 반환
    // - delete(user): User객체를 데이터베이스에서 삭제
    fun findByEmail(email: String): User?
}