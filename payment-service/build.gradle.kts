plugins {
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.msa"
version = "0.0.1-SNAPSHOT"
description = "payment-service"

dependencies {
    // payment-service 고유 의존성
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // 카프카 & Avro
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.confluent:kafka-avro-serializer:7.5.0")
    implementation("org.apache.avro:avro:1.11.3")

    // 테스트
    testImplementation("org.springframework.security:spring-security-test")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
