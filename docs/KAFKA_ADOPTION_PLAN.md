# Apache Kafka 도입안

## 문서 정보
- **작성일**: 2025-11-23
- **프로젝트**: Payment-Order-Event
- **대상 시스템**: 마이크로서비스 아키텍처 (Order, Payment, User Service)

---

## 1. 프로젝트 현황 분석

### 1.1 시스템 아키텍처

현재 시스템은 **이벤트 주도 마이크로서비스 아키텍처**를 목표로 구성된 3개의 독립 서비스로 구성되어 있습니다:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Order Service  │     │ Payment Service │     │  User Service   │
│   (Port 8080)   │     │   (Port 8080)   │     │   (Port 8081)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                         ┌───────▼────────┐
                         │ Apache Kafka   │
                         │ (localhost:9092)│
                         └────────────────┘
```

### 1.2 데이터베이스 구조

- **PostgreSQL** 단일 인스턴스 사용
  - 스키마 `payment-order`: Order Service + Payment Service 공유
  - 데이터베이스 `user_db`: User Service 전용

### 1.3 현재 Kafka 인프라 상태

#### ✅ 이미 구축된 환경

Docker Compose를 통해 다음 인프라가 구성되어 있습니다:

| 컴포넌트 | 이미지 | 포트 | 용도 |
|---------|--------|------|------|
| Zookeeper | confluentinc/cp-zookeeper:7.6.0 | 2181 | Kafka 클러스터 메타데이터 관리 |
| Kafka Broker | confluentinc/cp-kafka:7.6.0 | 9092, 29092 | 메시지 브로커 |
| Schema Registry | confluentinc/cp-schema-registry:7.6.0 | 8081 | Avro 스키마 관리 |

#### 🔧 미리 생성된 토픽

| 토픽명 | 파티션 | 용도 |
|--------|--------|------|
| order.events | 6 | 주문 생성/변경 이벤트 |
| payment.events | 6 | 결제 처리 이벤트 |
| inventory.events | 6 | 재고 변동 이벤트 |
| notification.events | 3 | 알림 발송 이벤트 |
| order.events.DLQ | 3 | Dead Letter Queue (실패 메시지) |

### 1.4 서비스별 Kafka 구현 현황

#### Order Service (Producer만 구현)
- **구현 상태**: ✅ Kafka Producer 설정 완료
- **설정 파일**: `order-service/src/main/kotlin/com/msa/order/config/KafkaConfig.kt`
- **주요 설정**:
  - Serializer: StringSerializer
  - acks=all (모든 ISR 확인)
  - enable.idempotence=true (멱등성 보장)
- **현재 동작**:
  - 애플리케이션 시작 시 `order-events` 토픽에 헬스체크 메시지 발송
  - `OrderEventsSmokeProducer`를 통한 연결 확인

#### Payment Service (Producer + Consumer 설정)
- **구현 상태**: ⚠️ 설정만 존재, Consumer 미구현
- **Producer 설정**:
  - Serializer: KafkaAvroSerializer (스키마 레지스트리 연동)
  - Schema Registry URL: http://localhost:8081
  - acks=all, idempotence=true
- **Consumer 설정**:
  - Group ID: payment-order-group
  - Deserializer: JsonDeserializer
  - auto-offset-reset: earliest

#### User Service
- **구현 상태**: ❌ Kafka 설정 없음
- **현재 통신 방식**: REST API만 제공

---

## 2. Kafka 도입 필요성 및 목표

### 2.1 현재 시스템의 문제점

#### 1️⃣ 동기 호출 기반 강결합
현재 서비스 간 통신이 REST API 동기 호출로 구성되어 있어:
- 한 서비스 장애 시 연쇄 장애 발생 위험
- 응답 시간이 모든 서비스의 합산 시간으로 증가
- 서비스 간 의존성이 높아 독립 배포 어려움

#### 2️⃣ 데이터 일관성 문제
- **DB 트랜잭션**과 **Kafka 메시지 발행**이 원자적으로 처리되지 않음
- DB 저장은 성공했으나 Kafka 발행이 실패하는 경우 데이터 불일치 발생
- README에 언급된 **Outbox 패턴이 미구현** 상태

#### 3️⃣ Consumer 미구현
- Producer 설정은 되어 있으나 실제 이벤트를 소비하는 로직 부재
- 발행된 이벤트가 활용되지 않음
- 이벤트 주도 아키텍처의 장점 미활용

### 2.2 Kafka 도입 목표

#### 비즈니스 목표
1. **서비스 간 느슨한 결합**: 각 서비스가 독립적으로 배포/운영 가능
2. **장애 격리**: 특정 서비스 장애가 전체 시스템에 영향을 주지 않음
3. **확장성**: 트래픽 증가 시 서비스별 독립적 확장
4. **데이터 일관성**: Outbox 패턴으로 분산 트랜잭션 보장

#### 기술 목표
1. **완전한 이벤트 주도 아키텍처 구현**
2. **Outbox 패턴 적용**으로 메시지 전송 보장
3. **Consumer 구현**으로 이벤트 체인 완성
4. **모니터링 및 DLQ 처리** 메커니즘 구축

---

## 3. 도입 아키텍처 설계

### 3.1 이벤트 플로우

```
[사용자 주문 요청]
      │
      ▼
┌─────────────────────────────────────────────┐
│ Order Service                                │
│ 1. Order 생성 & Outbox 테이블 저장 (트랜잭션) │
│ 2. Outbox Relay가 메시지를 Kafka로 발행     │
└─────────────────────────────────────────────┘
      │
      │ order.events
      ▼
┌─────────────────────────────────────────────┐
│ Payment Service (Consumer)                   │
│ 1. OrderCreated 이벤트 수신                  │
│ 2. 결제 처리 & Payment 저장                  │
│ 3. payment.events 발행                       │
└─────────────────────────────────────────────┘
      │
      ├─► inventory.events → Inventory Service (미래 확장)
      │
      └─► notification.events
            │
            ▼
      ┌──────────────────────┐
      │ Notification Service │
      │ (이메일/SMS 발송)      │
      └──────────────────────┘
```

### 3.2 Outbox 패턴 적용

#### 문제 상황
```kotlin
// ❌ 잘못된 구현: 트랜잭션 불일치 가능
@Transactional
fun createOrder(request: OrderRequest) {
    orderRepository.save(order) // DB 저장 성공
    kafkaTemplate.send("order.events", event) // Kafka 발행 실패 시?
}
```

#### Outbox 패턴 솔루션

**1단계: Outbox 테이블 생성**
```sql
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(published, created_at);
```

**2단계: 트랜잭션 내 Outbox 저장**
```kotlin
@Transactional
fun createOrder(request: OrderRequest) {
    // 1. Order 엔티티 저장
    val order = orderRepository.save(Order(...))

    // 2. Outbox 이벤트 저장 (같은 트랜잭션)
    outboxRepository.save(OutboxEvent(
        aggregateType = "Order",
        aggregateId = order.id,
        eventType = "OrderCreated",
        payload = objectMapper.writeValueAsString(OrderCreatedEvent(order))
    ))
    // ✅ DB 커밋 시 Order와 OutboxEvent가 원자적으로 저장됨
}
```

**3단계: Outbox Relay (별도 스레드/스케줄러)**
```kotlin
@Scheduled(fixedDelay = 1000) // 1초마다 실행
fun relayOutboxEvents() {
    val unpublishedEvents = outboxRepository.findByPublishedFalse()

    unpublishedEvents.forEach { event ->
        try {
            kafkaTemplate.send(
                "${event.aggregateType.lowercase()}.events",
                event.payload
            ).get() // 동기 대기

            // 발행 성공 시 published = true 업데이트
            outboxRepository.markAsPublished(event.id)
        } catch (e: Exception) {
            log.error("Failed to publish event ${event.id}", e)
            // 재시도 로직 or DLQ 전송
        }
    }
}
```

### 3.3 Consumer 구현 전략

#### Payment Service Consumer 예시

```kotlin
@Component
class OrderEventConsumer(
    private val paymentService: PaymentService
) {

    @KafkaListener(
        topics = ["order.events"],
        groupId = "payment-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleOrderEvent(
        @Payload event: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        log.info("Received event from $topic at offset $offset: $event")

        try {
            when (event.eventType) {
                "OrderCreated" -> paymentService.processPayment(event)
                "OrderCancelled" -> paymentService.refundPayment(event)
                else -> log.warn("Unknown event type: ${event.eventType}")
            }
        } catch (e: Exception) {
            log.error("Failed to process event", e)
            throw e // DLQ로 전송됨
        }
    }
}
```

### 3.4 에러 처리 및 재시도 전략

#### Dead Letter Queue (DLQ) 설정

```kotlin
@Configuration
class KafkaErrorHandlingConfig {

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory

        // 재시도 + DLQ 설정
        factory.setCommonErrorHandler(
            DefaultErrorHandler(
                DeadLetterPublishingRecoverer(kafkaTemplate),
                FixedBackOff(1000L, 3) // 1초 간격 3회 재시도
            )
        )

        return factory
    }
}
```

#### 모니터링 포인트
- Kafka Lag: 각 Consumer Group의 지연 시간
- DLQ 메시지 수: 비정상 이벤트 발생 추이
- Outbox 미발행 건수: Outbox Relay 성능 모니터링

---

## 4. 구현 로드맵

### Phase 1: 기반 구축 (1-2주)

#### 1.1 Outbox 패턴 구현
- [ ] Outbox 테이블 DDL 작성 (Flyway 마이그레이션)
- [ ] OutboxEvent 엔티티 및 Repository 구현
- [ ] OutboxRelay 스케줄러 구현
- [ ] 단위 테스트 작성 (Testcontainers 활용)

#### 1.2 Order Service 개선
- [ ] 주문 생성 로직에 Outbox 패턴 적용
- [ ] KafkaTemplate Bean 설정 개선
- [ ] 통합 테스트 작성

### Phase 2: Consumer 구현 (2-3주)

#### 2.1 Payment Service Consumer
- [ ] OrderEventConsumer 구현
  - OrderCreated 이벤트 처리
  - 결제 로직 실행
  - PaymentCompleted 이벤트 발행
- [ ] 에러 핸들링 및 DLQ 설정
- [ ] 멱등성 처리 (중복 이벤트 방지)

#### 2.2 User Service Kafka 통합
- [ ] Kafka 의존성 추가
- [ ] Consumer 설정 추가
- [ ] UserEventConsumer 구현 (사용자 정보 동기화)

### Phase 3: 고급 기능 (3-4주)

#### 3.1 Event Sourcing (선택)
- [ ] 이벤트 저장소 설계
- [ ] 이벤트 재생 메커니즘 구현
- [ ] CQRS 패턴 적용 검토

#### 3.2 Saga 패턴 (분산 트랜잭션)
- [ ] Orchestration vs Choreography 선택
- [ ] Compensation 로직 구현
- [ ] Saga 상태 관리

#### 3.3 모니터링 및 운영
- [ ] Kafka Exporter + Prometheus 연동
- [ ] Grafana 대시보드 구축
- [ ] 알림 규칙 설정 (Lag, Error Rate)

---

## 5. 기술 스택 정리

### 5.1 현재 사용 중인 Kafka 관련 기술

| 항목 | 기술 | 버전 |
|------|------|------|
| 메시지 브로커 | Apache Kafka | 7.6.0 (Confluent Platform) |
| Serialization | Avro, JSON, String | - |
| 스키마 관리 | Confluent Schema Registry | 7.6.0 |
| Spring 통합 | Spring Kafka | (Spring Boot 3.4.3 포함) |
| 테스트 | Testcontainers Kafka | - |

### 5.2 추가 고려 기술

#### Kafka Streams (실시간 스트림 처리)
- 사용 사례: 실시간 주문 집계, 이상 거래 탐지
- 장점: Kafka 내장, 별도 프레임워크 불필요
- 단점: 학습 곡선, 복잡한 상태 관리

#### Kafka Connect (외부 시스템 연동)
- 사용 사례: PostgreSQL CDC (Change Data Capture)
- 장점: Outbox 패턴 자동화 (Debezium Connector)
- 단점: 인프라 복잡도 증가

---

## 6. 예상 효과 및 리스크

### 6.1 기대 효과

#### 성능
- **응답 시간 단축**: 동기 호출 제거로 평균 200ms → 50ms
- **처리량 증가**: 비동기 처리로 초당 1000 TPS → 5000 TPS

#### 안정성
- **장애 격리**: 결제 서비스 장애 시에도 주문 접수 가능
- **데이터 정합성**: Outbox 패턴으로 메시지 유실 0%

#### 확장성
- **서비스 독립 배포**: 각 서비스 개별 배포 주기 운영
- **수평 확장**: Kafka 파티션 수 조절로 Consumer 확장 가능

### 6.2 리스크 및 대응 방안

| 리스크 | 영향도 | 대응 방안 |
|--------|--------|-----------|
| **Kafka 인프라 장애** | High | Multi-broker 클러스터 구성, 리플리케이션 팩터 3 이상 |
| **Outbox Relay 지연** | Medium | 병렬 처리, 배치 크기 조정, 모니터링 알림 |
| **중복 메시지 처리** | Medium | Consumer에 멱등성 키 구현 (이벤트 ID 기반 중복 체크) |
| **스키마 호환성 문제** | Low | Schema Registry 활용, 하위 호환성 규칙 준수 |
| **운영 복잡도 증가** | Medium | 문서화, 모니터링 자동화, 팀 교육 |

---

## 7. 테스트 전략

### 7.1 단위 테스트
```kotlin
@Test
fun `Outbox 이벤트가 트랜잭션과 함께 저장되어야 한다`() {
    // given
    val order = Order(...)

    // when
    orderService.createOrder(order)

    // then
    val savedOrder = orderRepository.findById(order.id)
    val outboxEvents = outboxRepository.findByAggregateId(order.id)

    assertThat(savedOrder).isNotNull
    assertThat(outboxEvents).hasSize(1)
    assertThat(outboxEvents[0].eventType).isEqualTo("OrderCreated")
}
```

### 7.2 통합 테스트 (Testcontainers)
```kotlin
@SpringBootTest
@Testcontainers
class KafkaIntegrationTest {

    @Container
    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))

    @Test
    fun `주문 생성 시 order-events 토픽에 메시지가 발행되어야 한다`() {
        // given
        val orderRequest = OrderRequest(...)

        // when
        orderController.createOrder(orderRequest)

        // then
        val records = kafkaConsumer.poll(Duration.ofSeconds(5))
        assertThat(records).hasSize(1)
        assertThat(records.first().topic()).isEqualTo("order.events")
    }
}
```

### 7.3 E2E 시나리오 테스트
1. 주문 생성 → Outbox 저장 확인
2. Outbox Relay → Kafka 발행 확인
3. Payment Consumer → 결제 처리 확인
4. Payment Event 발행 → Notification Consumer 확인

---

## 8. 참고 자료

### 공식 문서
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)
- [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html)

### 아키텍처 패턴
- [Outbox Pattern - Microservices.io](https://microservices.io/patterns/data/transactional-outbox.html)
- [Saga Pattern - Chris Richardson](https://microservices.io/patterns/data/saga.html)
- [Event Sourcing - Martin Fowler](https://martinfowler.com/eaaDev/EventSourcing.html)

### 관련 프로젝트
- [Debezium (CDC)](https://debezium.io/)
- [Kafka Streams](https://kafka.apache.org/documentation/streams/)

---

## 9. 의사결정 기록

| 날짜 | 결정 사항 | 이유 |
|------|-----------|------|
| 2025-11-23 | Outbox 패턴 우선 적용 | DB-Kafka 정합성 보장이 최우선 과제 |
| 2025-11-23 | Avro vs JSON: 혼용 | Payment(Avro), Order(String), User(JSON) 각 서비스 특성 고려 |
| 2025-11-23 | DLQ 파티션 3개 | 에러 메시지 처리 우선순위 낮음, 리소스 절약 |

---

## 10. 다음 단계

### 즉시 실행 가능한 작업
1. Outbox 테이블 DDL 작성 및 Flyway 마이그레이션 실행
2. Order Service에 OutboxEvent 엔티티 추가
3. Payment Service에 OrderEventConsumer 기본 구조 구현

### 추가 검토 필요
- User Service의 Kafka 역할 정의 (현재 REST API만 제공)
- Inventory Service 신규 개발 여부
- Notification Service 구현 범위 (이메일/SMS/푸시)

### 팀 논의 필요
- Saga 패턴 적용 시점 및 범위
- Event Sourcing 도입 여부
- 운영 환경 Kafka 클러스터 규모 (브로커 수, 파티션 전략)