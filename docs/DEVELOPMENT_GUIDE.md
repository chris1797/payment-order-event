# 개발 및 디버깅 가이드

## 프로젝트 구조

이 프로젝트는 **멀티모듈 마이크로서비스 아키텍처**로 구성되어 있습니다.

```
payment-order-event/
├── build.gradle.kts          # 공통 설정 (실행 불가)
├── docker-compose.yml         # Kafka 인프라
├── order-service/             # 주문 서비스 (독립 실행)
├── payment-service/           # 결제 서비스 (독립 실행)
└── user-service/              # 사용자 서비스 (독립 실행)
```

### 특징
- 루트 프로젝트: 공통 설정만 관리 (실행 불가)
- 각 서비스: 완전히 독립적으로 실행 가능
- 공통 의존성: 루트 `build.gradle.kts`의 `subprojects` 블록에서 관리

---

## 로컬 개발 환경 설정

### 1. 카프카 인프라 시작

```bash
# 카프카, Zookeeper, Schema Registry 시작
docker-compose up -d

# 상태 확인 (모두 Up 상태여야 함)
docker-compose ps

# 로그 확인
docker-compose logs -f kafka
```

**실행되는 서비스:**
- Zookeeper: `localhost:2181`
- Kafka: `localhost:9092`
- Schema Registry: `localhost:8081`

**자동 생성되는 토픽:**
- `order.events`
- `payment.events`
- `inventory.events`
- `notification.events`
- `order.events.DLQ`

---

### 2. 서비스 실행

각 서비스를 별도 터미널에서 실행:

```bash
# 터미널 1: order-service
./gradlew :order-service:bootRun

# 터미널 2: payment-service
./gradlew :payment-service:bootRun

# 터미널 3: user-service
./gradlew :user-service:bootRun
```

**서비스 포트:**
- order-service: `8080`
- payment-service: (설정 확인 필요)
- user-service: (설정 확인 필요)

---

## 카프카 이벤트 디버깅

### 방법 1: 엔드포인트로 메시지 발행

```bash
# order-service의 테스트 엔드포인트
curl http://localhost:8080/_probe/send?msg=order-1
```

### 방법 2: 카프카 콘솔로 메시지 확인 (추천)

특정 토픽의 메시지를 실시간으로 확인:

```bash
# order-events 토픽 메시지 확인
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning

# payment-events 토픽 메시지 확인
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic payment-events \
  --from-beginning
```

**옵션 설명:**
- `--from-beginning`: 토픽의 모든 메시지 확인 (생략 시 새 메시지만)
- `Ctrl+C`로 종료

---

## 디버깅 시나리오 예시

### 시나리오: Order 이벤트 발행 및 확인

```bash
# 1. 카프카 시작
docker-compose up -d

# 2. order-service 실행 (터미널 1)
./gradlew :order-service:bootRun

# 3. 카프카 메시지 모니터링 (터미널 2)
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning

# 4. 메시지 발행 (터미널 3)
curl http://localhost:8080/_probe/send?msg=test-order-1

# ✅ 터미널 2에서 메시지 확인 가능!
```

---

## 유용한 카프카 명령어

### 토픽 관리

```bash
# 토픽 목록 확인
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# 특정 토픽 상세 정보
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic order-events

# 토픽 생성 (수동)
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic my-topic \
  --partitions 3 \
  --replication-factor 1

# 토픽 삭제
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic my-topic
```

### Consumer Group 확인

```bash
# Consumer Group 목록
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# 특정 Consumer Group 상세 정보
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group my-consumer-group
```

---

## Kafka UI 추가 (선택사항)

웹 UI로 토픽과 메시지를 더 편하게 확인하려면 `docker-compose.yml`에 추가:

```yaml
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
```

추가 후:
```bash
docker-compose up -d
```

브라우저에서 `http://localhost:8090` 접속하여 확인 가능.

---

## 빌드 명령어

### 전체 프로젝트 빌드

```bash
# 테스트 포함 빌드
./gradlew clean build

# 테스트 제외 빌드 (빠름)
./gradlew clean build -x test
```

### 특정 모듈만 빌드

```bash
./gradlew :order-service:clean :order-service:build -x test
./gradlew :payment-service:clean :payment-service:build -x test
./gradlew :user-service:clean :user-service:build -x test
```

---

## 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :order-service:test
./gradlew :payment-service:test
./gradlew :user-service:test
```

---

## 인프라 종료

```bash
# 카프카 인프라 종료
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v
```

---

## 트러블슈팅

### 1. 카프카 연결 실패

**증상:** 서비스에서 `Connection refused` 에러

**해결:**
```bash
# 카프카가 실행 중인지 확인
docker-compose ps

# 카프카 로그 확인
docker-compose logs kafka

# 재시작
docker-compose restart kafka
```

### 2. 포트 충돌

**증상:** `Address already in use` 에러

**해결:**
```bash
# 특정 포트 사용 확인 (예: 9092)
lsof -i :9092

# 프로세스 종료 후 재시작
kill -9 <PID>
docker-compose up -d
```

### 3. 토픽이 자동 생성되지 않음

**해결:**
```bash
# kafka-topics-init 컨테이너 로그 확인
docker-compose logs kafka-topics-init

# 수동으로 토픽 생성
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic order-events \
  --partitions 6 \
  --replication-factor 1
```

### 4. 404 에러 - 컨트롤러 로드 안 됨

**원인:** 애플리케이션 재시작 필요

**해결:**
```bash
# 서비스 재시작
./gradlew :order-service:bootRun
```

---

## 참고 자료

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Testcontainers](https://www.testcontainers.org/)
