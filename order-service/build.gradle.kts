plugins {
    id("com.github.davidmc24.gradle.plugin.avro")
}

group = "com.msa"
version = "0.0.1-SNAPSHOT"
description = "order-service"

dependencies {
    // order-service 고유 의존성
    implementation("org.springframework.kafka:spring-kafka")

    // Avro 의존성
    implementation("org.apache.avro:avro:1.11.3")
    implementation("io.confluent:kafka-avro-serializer:7.6.0")

    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:postgresql")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
