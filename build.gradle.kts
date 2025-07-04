plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.ticketing"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// 기본 의존성
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// JPA 및 데이터베이스 관련
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	// Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// 유효성 검증
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Swagger UI (API 문서)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// JWT 처리를 위한 라이브러리 (jjwt)
	// JWT의 인터페이스와 기본 클래스를 제공합니다.
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	// JWT의 실제 구현체를 제공합니다.
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	// JWT를 JSON으로 변환하거나 그 반대의 작업을 위해 Jackson 라이브러리와의 통합을 지원합니다.
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Spring 애플리케이션에서 Redis를 쉽게 사용할 수 있도록 도와주는 라이브러리입니다.
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	
	// 테스트용 기본 의존성
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
