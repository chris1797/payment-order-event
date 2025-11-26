package com.msa.order.infra

import com.msa.order.events.OrderCreated
import org.springframework.boot.CommandLineRunner
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrderEventsSmokeProducer(
    private val kafkaTemplate: KafkaTemplate<String, OrderCreated>
): CommandLineRunner {

    // 애플리케이션 시작 시 Kafka 토픽에 메시지를 전송하여 연결 상태를 확인
    override fun run(vararg args: String?) {
        val topic = "order-events" // Kafka 토픽 이름

        // Avro 이벤트 생성
        val event = OrderCreated.newBuilder()
            .setOrderId(UUID.randomUUID().toString())
            .setOrderCode("SMOKE-TEST")
            .setTimestamp(System.currentTimeMillis())
            .setEventType("ORDER_CREATED")
            .build()

        // 메시지 전송
        kafkaTemplate.send(topic, event.orderId.toString(), event)
            .whenComplete { _, ex ->
                if (ex != null) {
                    println("❌ Failed to send message: ${ex.message}")
                } else {
                    println("✅ Successfully sent OrderCreated event to topic '$topic': ${event.orderCode}")
                }
            }
    }
}