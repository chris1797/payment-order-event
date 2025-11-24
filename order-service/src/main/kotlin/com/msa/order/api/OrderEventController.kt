package com.msa.order.api

import com.msa.order.events.OrderCreated
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class OrderEventController(
    private val kafkaTemplate: KafkaTemplate<String, OrderCreated>,
) {

    @GetMapping("/test")
    fun test(): String {
        return "Controller is working!"
    }

    @GetMapping("/_probe/send")
    fun sendTestMessage(@RequestParam orderCode: String?): String {
        val topic = "order-events" // Kafka 토픽 이름

        // Avro 이벤트 생성
        val event = OrderCreated.newBuilder()
            .setOrderId(UUID.randomUUID().toString())
            .setOrderCode(orderCode ?: "PROBE-TEST")
            .setTimestamp(System.currentTimeMillis())
            .setEventType("ORDER_CREATED")
            .build()

        // 메시지 전송
        kafkaTemplate.send(topic, event.getOrderId().toString(), event)

        return "Sent OrderCreated event to topic '$topic': ${event.getOrderCode()}"
    }
}